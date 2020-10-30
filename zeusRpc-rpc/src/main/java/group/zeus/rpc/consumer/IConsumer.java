package group.zeus.rpc.consumer;

import group.zeus.rpc.core.RpcProtocal;
import group.zeus.rpc.core.RpcRequestHanlder;
import group.zeus.spi.SPI;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadPoolExecutor;

@SPI("netty")
public interface IConsumer {

    /*当前有效地址，不一定已经建立连接*/
    CopyOnWriteArrayList<RpcProtocal> VALID_RPCPROTOCALS = new CopyOnWriteArrayList<>();

    /*有效连接，RpcRequestHandler是接口规范*/
    Map<RpcProtocal, RpcRequestHanlder> VALID_CONNECT_NODES = new ConcurrentHashMap<>(128);

    /**
     * 多线程提交连接任务，建立一个有效连接
     * @param Connect_Nodes  有效连接节点集合
     * @param threadPoolExecutor 线程池用于提交连接任务
     */
    void connect(Map<RpcProtocal, RpcRequestHanlder> Connect_Nodes, ThreadPoolExecutor threadPoolExecutor);

    /**
     * 销毁资源，包括线程池
     * @param threadPoolExecutor 线程池
     */
    void destory(ThreadPoolExecutor threadPoolExecutor);

    /**
     * 创建代理,暂时不考虑CGLIB
     * @param interfaceClass 接口Class
     * @param version 服务版本号
     * @return 代理对象
     */
    <T> T createProxy(Class<T> interfaceClass, String version);
}
