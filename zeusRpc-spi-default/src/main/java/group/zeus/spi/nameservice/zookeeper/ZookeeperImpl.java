package group.zeus.spi.nameservice.zookeeper;

import group.zeus.rpc.core.INameService;
import group.zeus.rpc.dto.RpcProtocol;
import group.zeus.rpc.dto.RpcServiceInfo;
import group.zeus.rpc.util.ServiceUtils;
import group.zeus.spi.nameservice.NameServiceUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.framework.state.ConnectionStateListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Zookeeper实现，支持动态感知上下线
 * @Author: maodazhan
 * @Date: 2020/10/30 15:53
 */
public class ZookeeperImpl implements INameService {

    private static final Logger logger = LoggerFactory.getLogger(ZookeeperImpl.class);
    private CuratorClient curatorClient;
    private List<String> pathList = new ArrayList<>();

    /*可以并发修改，当前有效地址*/
    public static final CopyOnWriteArrayList<RpcProtocol> VALID_RPCPROTOCOLS = new CopyOnWriteArrayList<>();

    private ReentrantLock lock = new ReentrantLock();
    private Condition connect = lock.newCondition();
    private long waitTimeout = 5000;

    private volatile boolean isRunning = true;

    @Override
    public void register(String registryAddress, String serviceAddress, Map<String, Object> serviceMap) {
        curatorClient = new CuratorClient(registryAddress);
        // service info list
        List<RpcServiceInfo> serviceInfoList = NameServiceUtils.getRpcServiceInfos(serviceMap);
        try {
            RpcProtocol rpcProtocol = NameServiceUtils.getRpcProtocol(serviceAddress, serviceInfoList);
            String serviceData = rpcProtocol.toJson();
            byte[] bytes = serviceData.getBytes();
            String path = ZkConstants.ZK_DATA_PATH + "-" + rpcProtocol.hashCode();
            doRegister(path, bytes, serviceInfoList.size(), rpcProtocol);
            pathList.add(path);
        } catch (Exception ex) {
            logger.error("Register service fail, exception: {}", ex.getMessage());
        }

    }

    private void doRegister(String path, byte[] bytes, int serviceNums, RpcProtocol rpcProtocol){
        try {
            this.curatorClient.createPathData(path, bytes);
            logger.info("Register {} new service, host: {}, port: {}", serviceNums, rpcProtocol.getHost(), rpcProtocol.getPort());
        }catch (Exception ex){
            logger.error("Register service fail, exception: {}", ex.getMessage());
        }
        // FIXME zk上存在非常多的节点，似乎是本地开了太多的线程去连接它
        this.curatorClient.addConnectionStateListener(new ConnectionStateListener() {
            @Override
            public void stateChanged(CuratorFramework client, ConnectionState newState) {
                if (newState == ConnectionState.RECONNECTED) {
                    logger.info("Connection state:{}, register service after reconnected", newState);
                    doRegister(path, bytes, serviceNums, rpcProtocol);
                }
            }
        });
    }

    @Override
    public void discover(String discoverAddress) {
        try {
            curatorClient = new CuratorClient(discoverAddress);
            // Get initial service info
            logger.info("Get initial service info");
            // 修改rpcProtocols
            updateProtocol(VALID_RPCPROTOCOLS);
            // Add watch listener
            curatorClient.watchPathChildrenCache(ZkConstants.ZK_RPC_PATH, new PathChildrenCacheListener() {
                @Override
                public void childEvent(CuratorFramework client, PathChildrenCacheEvent event) throws Exception {
                    PathChildrenCacheEvent.Type type = event.getType();
                    switch (type) {
                        case CONNECTION_RECONNECTED:
                            logger.info("Reconnected to zk, try to get latest service list");
                            updateProtocol(VALID_RPCPROTOCOLS);
                            break;
                        case CHILD_ADDED:
                        case CHILD_UPDATED:
                        case CHILD_REMOVED:
                            logger.info("Service info changed, try to get latest service list");
                            // 修改rpcProtocols
                            updateProtocol(VALID_RPCPROTOCOLS);
                            break;
                    }
                }
            });
        } catch (Exception ex) {
            logger.error("Watch node exception: " + ex.getMessage());
        }

    }

    @Override
    public void stop() {
        isRunning = false;
        this.curatorClient.close();
        logger.info("Connection with registry ZK has been closed");
    }

    /*动态感知上下线，更新RpcProtocols*/
    public void updateProtocol(CopyOnWriteArrayList<RpcProtocol> curRpcProtocols) {
        try {
            List<String> nodeList = curatorClient.getChildren(ZkConstants.ZK_RPC_PATH);
            List<RpcProtocol> dataList = new ArrayList<>();
            for (String node : nodeList) {
                logger.debug("Service node: " + node);
                byte[] bytes = curatorClient.getData(ZkConstants.ZK_RPC_PATH + "/" + node);
                String json = new String(bytes);
                RpcProtocol rpcProtocol = RpcProtocol.fromJson(json);
                dataList.add(rpcProtocol);
            }
            logger.debug("Service node data: {}", dataList);
            if (dataList == null || dataList.size() == 0) {
                logger.error("No available service");
                curRpcProtocols.clear();
            } else {
                HashSet<RpcProtocol> serviceSet = new HashSet<>(dataList);
                // add incoming
                for (RpcProtocol rpcProtocol : serviceSet) {
                    if (!curRpcProtocols.contains(rpcProtocol)) {
                        logger.info("Add new service: " + rpcProtocol.toJson());
                        curRpcProtocols.add(rpcProtocol);
                    }
                }
                // remove invalid
                for (RpcProtocol rpcProtocol : curRpcProtocols){
                    if (!serviceSet.contains(rpcProtocol)){
                        logger.info("Remove valid service: " + rpcProtocol.toJson());
                        curRpcProtocols.remove(rpcProtocol);
                    }
                }
                signalAvailableProtocol();
            }
        } catch (Exception ex) {
            logger.error("Get node exception: " + ex.getMessage());
        }
    }

    @Override
    public List<RpcProtocol> getNewestProtocols() {
        while (isRunning == true && VALID_RPCPROTOCOLS.size() <= 0) {
            try {
                waitingForProtocol();
            } catch(InterruptedException ex){
                logger.error("Waiting for available service is interrupted");
            }
        }
        return VALID_RPCPROTOCOLS;
    }

    /*阻塞等待有效连接*/
    private void waitingForProtocol() throws InterruptedException{
        lock.lock();
        try{
            logger.warn("Waiting for available service");
            // 所有线程都将阻塞
            connect.await(this.waitTimeout, TimeUnit.MILLISECONDS);
        } finally {
            lock.unlock();
        }
    }

    public void signalAvailableProtocol(){
        lock.lock();
        try{
            // 唤醒所有阻塞的线程
            connect.signalAll();
        } finally {
            lock.unlock();
        }
    }
}
