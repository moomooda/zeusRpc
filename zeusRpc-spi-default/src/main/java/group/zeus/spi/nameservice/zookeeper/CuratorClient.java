package group.zeus.spi.nameservice.zookeeper;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.framework.state.ConnectionStateListener;
import org.apache.zookeeper.CreateMode;

import java.util.List;

/**
 * @Author: maodazhan
 * @Date: 2020/11/1 10:52
 */
public class CuratorClient {

    private CuratorFramework client;

    public CuratorClient(String connectString, String namespace, int sessionTimeout, int connectionTimeout){
        client = CuratorFrameworkFactory.builder().namespace(namespace).connectString(connectString)
                .sessionTimeoutMs(sessionTimeout).connectionTimeoutMs(connectionTimeout).retryPolicy(new ExponentialBackoffRetry(1000,10)).build();
        client.start();
    }

    public CuratorClient(String connectString){
        this(connectString, ZkConstants.ZK_NAMESPACE, ZkConstants.ZK_SESSION_TIMEOUT, ZkConstants.ZK_CONNECTION_TIMEOUT);
    }

    public CuratorFramework getClient(){
        return client;
    }

    public void close(){
        client.close();
    }

    List<String> getChildren(String path) throws Exception{
        return client.getChildren().forPath(path);
    }

    byte[] getData(String path) throws Exception{
        return client.getData().forPath(path);
    }

    /*避免了反复注册watcher*/
    /*对事件的监听，可以看作是本地缓存视图和远程ZK视图的对比过程*/
    public void watchPathChildrenCache(String path, PathChildrenCacheListener listener) throws Exception{
        PathChildrenCache pathChildrenCache = new PathChildrenCache(client, path, true);
        pathChildrenCache.start(PathChildrenCache.StartMode.BUILD_INITIAL_CACHE);
        pathChildrenCache.getListenable().addListener(listener);
    }

    public void addConnectionStateListener(ConnectionStateListener connectionStateListener){
        client.getConnectionStateListenable().addListener(connectionStateListener);
    }

    public void createPathData(String path, byte[] data) throws Exception{
        client.create().creatingParentContainersIfNeeded().withMode(CreateMode.EPHEMERAL_SEQUENTIAL)
                .forPath(path, data);
    }
}
