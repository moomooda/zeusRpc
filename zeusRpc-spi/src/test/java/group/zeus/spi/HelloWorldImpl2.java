package group.zeus.spi;

/**
 * @Author: maodazhan
 * @Date: 2020/10/28 18:17
 */
public class HelloWorldImpl2 implements HelloWorld{
    @Override
    public void sayHello() {
        System.out.println("Hello, I'm on behalf of one extension implementation of Interface HelloWord, named `hello2`");
    }
}
