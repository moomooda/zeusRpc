package group.zeus.rpc.core;

import group.zeus.rpc.util.ServiceUtils;
import group.zeus.rpc.annotation.RpcService;
import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.HashMap;
import java.util.Map;


/**
 * @Author: maodazhan
 * @Date: 2020/10/28 16:08
 */
// TODO setApplication完成于Bean实例化之后，BeanpostBefore之前，after之前
public class RpcServer implements ApplicationContextAware, InitializingBean, DisposableBean {

    /*用于通信连接*/
    private IProvider iProvider;

    /*用于服务注册*/
    private INameService iNameService;

    /*服务export管理*/
    private Map<String, Object> serviceMap = new HashMap<>();

    private String serverAddress;
    private String registryAddress;

    public RpcServer(String serverAddress,  String registryAddress, IProvider iProvider, INameService iNameService){
        this.serverAddress = serverAddress;
        this.registryAddress = registryAddress;
        this.iProvider = iProvider;
        this.iNameService = iNameService;
    }

    /*优雅shutdown*/
    @Override
    public void destroy() throws Exception {
        iProvider.stop();
        iNameService.stop();
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        /*iProvider和iNameService已经注入完毕*/
        iProvider.start(serverAddress, serviceMap);
        iNameService.register(registryAddress,serverAddress,serviceMap);
    }

    @Override
    public void setApplicationContext(ApplicationContext ctx) throws BeansException {
        /*@RpcService服务export管理*/
        Map<String, Object> serviceBeanMap = ctx.getBeansWithAnnotation(RpcService.class);
        if (MapUtils.isNotEmpty(serviceBeanMap)){
            serviceBeanMap.forEach((name, serviceBean)-> {
                RpcService rpcService = serviceBean.getClass().getAnnotation(RpcService.class);
                String interfaceName = rpcService.value().getName();
                String version = rpcService.version();
                ServiceUtils.addService(serviceMap, interfaceName, version, serviceBean);
            });
        }
    }
}
