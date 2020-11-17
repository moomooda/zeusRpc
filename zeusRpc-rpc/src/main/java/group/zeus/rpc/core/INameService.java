package group.zeus.rpc.core;

import group.zeus.rpc.dto.RpcProtocol;
import group.zeus.spi.SPI;

import java.util.List;
import java.util.Map;

/**
 * 注册中心：服务注册和发现
 * @Author: maodazhan
 * @Date: 2020/10/30 9:35
 */
@SPI("zookeeper")
public interface INameService {

    /**
     * 开启客户端，服务注册
     * @param registryAddress 注册中心地址ip:host
     * @param serviceAddress 服务暴露地址ip:host
     * @param serviceMap serviceKey : serviceBean
     */
    void register(String registryAddress, String serviceAddress, Map<String, Object> serviceMap);

    /**
     * 开启客户端，服务发现
     * @param discoverAddress 注册中心地址ip:host
     */
    void discover(String discoverAddress);

    /*获取最新的RpcProtocols*/
    List<RpcProtocol> getNewestProtocols();

    /*关闭客户端*/
    void stop();

}
