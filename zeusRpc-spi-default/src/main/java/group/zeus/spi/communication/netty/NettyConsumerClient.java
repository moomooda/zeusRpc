package group.zeus.spi.communication.netty;

import group.zeus.rpc.consumer.AbstractConsumer;
import group.zeus.rpc.core.RpcProtocal;
import group.zeus.rpc.core.RpcRequestHanlder;

import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @Author: maodazhan
 * @Date: 2020/10/28 15:20
 */
// TODO 这里充当NettyRpc的连接管理器的功能
// TODO 意味着http的方式也需要做连接管理器
// TODO 那tomcat那边的handler什么什么呢
// TODO 做好服务连接的管理就行了，一个是
public class NettyConsumerClient extends AbstractConsumer {

    @Override
    public void connect(Map<RpcProtocal, RpcRequestHanlder> Connect_Nodes, ThreadPoolExecutor threadPoolExecutor) {

    }

    @Override
    public void destory(ThreadPoolExecutor threadPoolExecutor) {

    }
}
