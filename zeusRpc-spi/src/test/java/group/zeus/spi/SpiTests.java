package group.zeus.spi;


import org.junit.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * @Author: maodazhan
 * @Date: 2020/10/11 13:47
 */
public class SpiTests {

    @Test
    public void testGetExtension1(){
        //  case-insensitive
        HelloWorld instance = SpiFactory.getExtension(HelloWorld.class, "hello1");
        HelloWorld instance2 = SpiFactory.getExtension(HelloWorld.class, "HELLO1");
//        HelloWorld instance3 = SpiFactory.getExtension(HelloWorld.class, "hello2");
        instance.sayHello();
        instance2.sayHello();
    }

    @Test
    public void testGetExtension2(){
        // get default extension impl instance
        HelloWorld instance = SpiFactory.getExtension(HelloWorld.class, "default");
        instance.sayHello();
    }

    @Test
    public void testSpringSPI(){
        AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext(TestSpiConfiguration.class);
        applicationContext.getBean(HelloWorld.class).sayHello();
    }

}
