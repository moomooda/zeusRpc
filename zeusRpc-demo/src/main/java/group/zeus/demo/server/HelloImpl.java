package group.zeus.demo.server;

import group.zeus.demo.api.HelloService;
import group.zeus.demo.api.Person;
import group.zeus.rpc.annotation.RpcService;

/**
 * @Author: maodazhan
 * @Date: 2020/11/9 10:25
 */
@RpcService(value =  HelloService.class, version = "1.0")
public class HelloImpl implements HelloService{

    @Override
    public String hello(String name) {
        return "Hello " + name;
    }

    @Override
    public String hello(Person person) {
        return "Hello " + person.getFirstName() + " " + person.getLastName();
    }
}
