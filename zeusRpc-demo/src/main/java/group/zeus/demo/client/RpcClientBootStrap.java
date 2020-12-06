package group.zeus.demo.client;

import group.zeus.demo.api.Person;
import group.zeus.rpc.RpcClientConfiguration;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * @Author: maodazhan
 * @Date: 2020/12/5 22:25
 */
public class RpcClientBootStrap {
    public static void main(String[] args) {
        AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext(RpcClientConfiguration.class, ClientApplicationConfiguration.class);
        // 确保销毁资源
        applicationContext.registerShutdownHook();
        WelcomeImpl welcome = applicationContext.getBean(WelcomeImpl.class);
        welcome.welcome(new Person("dazhan", "mao"));
        welcome.welcome("maodazhan");

        // 测试心跳
        try {
            Thread.sleep(150000);
        } catch (Exception ex){
            //
        }
    }
}
