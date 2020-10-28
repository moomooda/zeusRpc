package group.zeus.spi;

/**
 * @Author: maodazhan
 * @Date: 2020/10/11 13:43
 */
public class HelloWorldImpl implements HelloWorld{
    @Override
    public void sayHello() {
        System.out.println("Hello, I'm on behalf of one extension implementation of Interface HelloWord, named `hello1`");
    }
}
