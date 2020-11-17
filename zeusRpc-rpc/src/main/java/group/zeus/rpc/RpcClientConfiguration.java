package group.zeus.rpc;

import group.zeus.rpc.core.IConsumer;
import group.zeus.rpc.core.ILoadBalance;
import group.zeus.rpc.core.INameService;
import group.zeus.rpc.core.RpcClient;
import group.zeus.spi.SpiFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/**
 * 用于加载RpcClient
 * @Author: maodazhan
 * @Date: 2020/10/31 16:03
 */
@Configuration
@PropertySource(value={"classpath:/application.properties"})
public class RpcClientConfiguration {

    @Value("${zeusRpc.IConsumer}")
    private String consumer;

    @Value("${zeusRpc.INameService}")
    private String name_service;

    @Value("${zeusRpc.ILoadBalance}")
    private String load_balance;

    @Value("${zeusRpc.discoverAddress")
    private String discoverAddress;

    @Bean
    public IConsumer iConsumer(){
        if (!consumer.equals("${zeusRpc.IConsumer}"))
            return SpiFactory.getExtension(IConsumer.class, consumer);
        else
            return SpiFactory.getExtension(IConsumer.class, "default");
    }

    @Bean
    public INameService iNameService(){
        if (!name_service.equals("${zeusRpc.INameService}"))
            return SpiFactory.getExtension(INameService.class, name_service);
        else
            return SpiFactory.getExtension(INameService.class, "default");
    }

    @Bean
    public ILoadBalance iLoadBalance(){
        if (!load_balance.equals("${zeusRpc.ILoadBalance}"))
            return SpiFactory.getExtension(ILoadBalance.class, load_balance);
        else
            return SpiFactory.getExtension(ILoadBalance.class, "default");
    }

    @Bean("rpc_client")
    public RpcClient rpcClient(IConsumer iConsumer, INameService iNameService, ILoadBalance iLoadBalance){
        return new RpcClient(discoverAddress, iConsumer, iNameService, iLoadBalance);
    }
}
