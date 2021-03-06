package group.zeus.demo.server;

import group.zeus.demo.api.WelcomeService;
import group.zeus.demo.api.Person;
import group.zeus.rpc.annotation.RpcService;

/**
 * @Author: maodazhan
 * @Date: 2020/11/9 10:25
 */
@RpcService(value =  WelcomeService.class, version = "1.0")
public class HelloImpl implements WelcomeService {

    @Override
    public String welcome(String name) {
        return "Hello " + name;
    }

    @Override
    public String welcome(Person person) {
        return "Hello " + person.getFirstName() + " " + person.getLastName();
    }
}
