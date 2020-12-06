package group.zeus.demo.client;

import group.zeus.demo.api.Person;
import group.zeus.demo.api.WelcomeService;
import group.zeus.rpc.annotation.RpcAutowired;
import org.springframework.stereotype.Component;

/**
 * @Author: maodazhan
 * @Date: 2020/12/6 10:38
 */
@Component
public class WelcomeImpl {

    @RpcAutowired(version = "2.0")
    WelcomeService welcomeService;

    public void welcome(String name){
        System.out.println(welcomeService.welcome(name));
    }

    public void welcome(Person person){
        System.out.println(welcomeService.welcome(person));
    }

}
