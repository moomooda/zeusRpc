package group.zeus.rpc.nameservice;

import group.zeus.rpc.core.RpcProtocal;
import group.zeus.spi.SPI;

import java.util.Map;

/**
 * 注册中心：服务注册和发现
 * @Author: maodazhan
 * @Date: 2020/10/30 9:35
 */
@SPI("zookeeper")
public interface INamingService {

    /**
     *服务注册
     * @param providerAddress 服务端地址ip:host
     * @param serviceMap serviceKey : serviceBean
     */
    void register(String providerAddress, Map<String, Object> serviceMap);

    /**
     * 服务发现
     * @param serviceKey
     * @return 服务地址{host, port, serviceInfoLists)
     */
    RpcProtocal discover(String serviceKey);

}
