package group.zeus.demo.server;

import group.zeus.demo.api.WelcomeService;
import group.zeus.rpc.core.INameService;
import group.zeus.rpc.core.IProvider;
import group.zeus.rpc.util.ServiceUtils;
import group.zeus.spi.SpiFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Netty+zookeeper+一致性hash
 * Tomcat+redis+roundRobin
 * 不使用Spring驱动
 * @Author: maodazhan
 * @Date: 2020/11/9 10:17
 */
public class RpcServerTest {

    private static final Logger logger = LoggerFactory.getLogger(RpcServerTest.class);

    public static void main(String[] args) {
        Map<String, Object> serviceMap = new HashMap<>();
//        IProvider provider = SpiFactory.getExtension(IProvider.class, "default");
        IProvider provider = SpiFactory.getExtension(IProvider.class, "tomcat");
//        INameService nameService = SpiFactory.getExtension(INameService.class, "default");
        INameService nameService = SpiFactory.getExtension(INameService.class, "redis");
        String serverAddress = "127.0.0.1:18877";
//        String registryAddress = "222.28.84.14:2181";
        String registryAddress = "222.28.84.14:6379";
        WelcomeService welcomeService = new HelloImpl();
        ServiceUtils.addService(serviceMap, WelcomeService.class.getName(), "1.0", welcomeService);
        WelcomeService welcomeService2 = new HiImpl();
        ServiceUtils.addService(serviceMap, WelcomeService.class.getName(), "2.0", welcomeService2);
        try {
            // TODO provider应该依赖nameservice，解决spi依赖注入
            provider.start(serverAddress, serviceMap);
            nameService.register(registryAddress, serverAddress, serviceMap);
        } catch (Exception ex) {
            logger.error("Exception: {}", ex);
        }
        try {
            Thread.sleep(15000);
            logger.info("Time to test elapsed, start to close server");
            provider.stop();
            nameService.stop();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
