package group.zeus.demo.server;

import group.zeus.rpc.RpcServerConfiguration;
import group.zeus.rpc.core.RpcServer;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * @Author: maodazhan
 * @Date: 2020/11/9 10:01
 */
public class RpcServerBootStrap {
    public static void main(String[] args) {
        AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext(RpcServerConfiguration.class, ServerApplicationConfiguration.class);
        // 确保销毁资源
        applicationContext.registerShutdownHook();
    }
}
