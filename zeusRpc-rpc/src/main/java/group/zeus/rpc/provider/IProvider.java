package group.zeus.rpc.provider;

import group.zeus.spi.SPI;

@SPI("netty")
public interface IProvider {

    /*启动服务端*/
    void start();

    /*关闭服务端*/
    void stop();

    /**
     * 注册本地服务用于发布到注册中心
     * @param interfaceName 接口名
     * @param version 服务版本号
     * @param serviceBean 服务接口实例Bean
     */
    void addService(String interfaceName, String version, Object serviceBean);
}
