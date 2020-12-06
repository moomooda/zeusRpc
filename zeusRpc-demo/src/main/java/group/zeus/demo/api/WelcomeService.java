package group.zeus.demo.api;

/**
 * @Author: maodazhan
 * @Date: 2020/11/9 10:31
 */
public interface WelcomeService {
    String welcome(String name);

    String welcome(Person person);
}
