package group.zeus.spi;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/**
 * @Author: maodazhan
 * @Date: 2020/10/28 16:45
 */
@Configuration
@PropertySource(value={"classpath:/application.properties"})
public class TestSpiConfiguration {

    @Value("${zeusRpc.helloWorld}")
    private String configured_extension;

    @Bean
    public  HelloWorld getHelloWorld(){
        if (!configured_extension.equals("${zeusRpc.helloWorld}"))
            return SpiFactory.getExtension(HelloWorld.class, configured_extension);
        else
            return SpiFactory.getExtension(HelloWorld.class, "default");
    }
}
