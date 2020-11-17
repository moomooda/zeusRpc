package group.zeus.demo.api;

/**
 * @Author: maodazhan
 * @Date: 2020/11/9 10:31
 */
public interface HelloService {
    String hello(String name);

    String hello(Person person);
}
