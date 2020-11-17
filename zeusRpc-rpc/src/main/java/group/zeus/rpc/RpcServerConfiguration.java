package group.zeus.rpc;

import group.zeus.rpc.core.INameService;
import group.zeus.rpc.core.IProvider;
import group.zeus.rpc.core.RpcServer;
import group.zeus.spi.SpiFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/**
 * @Author: maodazhan
 * @Date: 2020/10/31 15:45
 */
@Configuration
@PropertySource(value={"classpath:/application.properties"})
public class RpcServerConfiguration {

    @Value("${zeusRpc.IProvider}")
    private String provider;

    @Value("${zeusRpc.INameService}")
    private String nameservice;

    @Value("${zeusRpc.serviceAddress")
    private String serviceAddress;

    @Value("${zeusRpc.registryAddress")
    private String registryAddress;

    @Bean
    public IProvider iProvider(){
        if (!provider.equals("${zeusRpc.IProvider}"))
            return SpiFactory.getExtension(IProvider.class, provider);
        else
            return SpiFactory.getExtension(IProvider.class, "default");
    }
    @Bean
    public INameService iNameService(){
        if (!nameservice.equals("${zeusRpc.INameService}"))
            return SpiFactory.getExtension(INameService.class, nameservice);
        else
            return SpiFactory.getExtension(INameService.class, "default");
    }
    @Bean("rpc_server")
    public RpcServer rpcServer(IProvider iProvider, INameService iNameService){
        return new RpcServer(serviceAddress, registryAddress, iProvider, iNameService);
    }
}
