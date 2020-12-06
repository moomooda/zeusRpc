package group.zeus.demo.server;

import group.zeus.demo.api.HelloService;
import group.zeus.demo.api.Person;
import group.zeus.rpc.annotation.RpcService;

/**
 * @Author: maodazhan
 * @Date: 2020/11/9 10:37
 */
@RpcService(value = HelloService.class, version = "2.0")
public class HelloImpl2 implements HelloService{

    @Override
    public String hello(String name) {
        return "Hi " + name;
    }

    @Override
    public String hello(Person person) {
        return "Hi " + person.getFirstName() + " " + person.getLastName();
    }
}
