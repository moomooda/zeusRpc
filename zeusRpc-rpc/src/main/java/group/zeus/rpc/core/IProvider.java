package group.zeus.rpc.core;

import group.zeus.spi.SPI;

import java.util.Map;

@SPI("netty")
public interface IProvider {

    /**
     * 启动服务端
     * @param serviceAddress 暴露服务的地址
     * @param handleMap 暴露的服务信息map
     * @throws Exception
     */
    void start(String serviceAddress, Map<String, Object> handleMap) throws Exception;

    /*关闭服务端*/
    void stop() throws Exception;
}
