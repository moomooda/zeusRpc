package group.zeus.demo.client;

import group.zeus.rpc.core.IConsumer;
import group.zeus.rpc.core.ILoadBalance;
import group.zeus.rpc.core.INameService;
import group.zeus.rpc.core.RpcClient;
import group.zeus.spi.SpiFactory;
import group.zeus.demo.api.HelloService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Netty+zookeeper+一致性hash
 * 不使用Spring驱动
 * @Author: maodazhan
 * @Date: 2020/11/15 19:11
 */
public class RpcClientTest {
    private static final Logger logger = LoggerFactory.getLogger(RpcClientTest.class);

    public static void main(String[] args) {
//        IConsumer consumer = SpiFactory.getExtension(IConsumer.class, "default");
        IConsumer consumer = SpiFactory.getExtension(IConsumer.class, "tomcat");
//        INameService nameService = SpiFactory.getExtension(INameService.class, "default");
        INameService nameService = SpiFactory.getExtension(INameService.class, "redis");
        ILoadBalance loadBalance = SpiFactory.getExtension(ILoadBalance.class, "default");
//        String discoverAddress = "222.28.84.14:2181";
        String discoverAddress = "222.28.84.14:6379";
        RpcClient rpcClient = new RpcClient(discoverAddress, consumer, nameService, loadBalance);
        nameService.discover(discoverAddress);
        HelloService helloService1 = rpcClient.createProxy(HelloService.class, "1.0");
        // done 只有一条tcp连接被创建
        for(int i=0;i<=100;i++) {
            System.out.println(helloService1.hello("maodazhan"));
        }
        try {
            nameService.stop();
            consumer.stop();
        } catch (Exception ex){
            logger.error("Error: ", ex.getMessage());
        }
    }
}
