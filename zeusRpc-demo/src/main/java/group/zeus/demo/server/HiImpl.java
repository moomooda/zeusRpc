package group.zeus.demo.server;

import group.zeus.demo.api.WelcomeService;
import group.zeus.demo.api.Person;
import group.zeus.rpc.annotation.RpcService;

/**
 * @Author: maodazhan
 * @Date: 2020/11/9 10:37
 */
@RpcService(value = WelcomeService.class, version = "2.0")
public class HiImpl implements WelcomeService {

    @Override
    public String welcome(String name) {
        return "Hi " + name;
    }

    @Override
    public String welcome(Person person) {
        return "Hi " + person.getFirstName() + " " + person.getLastName();
    }
}
