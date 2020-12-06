package group.zeus.rpc.core;

import group.zeus.rpc.annotation.RpcAutowired;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;

/**
 * TODO 屏蔽Netty逻辑
 * TODO 负载均衡的逻辑应该放这里调用
 * @Author: maodazhan
 * @Date: 2020/10/28 16:09
 */
public class RpcClient implements ApplicationContextAware, InitializingBean, DisposableBean {

    private static final Logger logger = LoggerFactory.getLogger(RpcClient.class);

    /*用于通信连接*/
    private IConsumer iConsumer;

    /*用于服务发现*/
    private INameService iNameService;

    /*用于负载均衡*/
    private ILoadBalance iLoadBalance;

    private String discoverAddress;

    public RpcClient(String address ,IConsumer iConsumer, INameService iNameService, ILoadBalance iLoadBalance){
        this.discoverAddress = address;
        this.iConsumer = iConsumer;
        this.iNameService = iNameService;
        this.iLoadBalance = iLoadBalance;
    }

    /*优雅shutdown*/
    @Override
    public void destroy() throws Exception {
        iConsumer.stop();
        iNameService.stop();
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        iNameService.discover(discoverAddress);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        /*@RpcAutowired 远程服务引用代理对象管理*/
        String[] beanNames = applicationContext.getBeanDefinitionNames();
        for(String beanName: beanNames){
            Object bean = applicationContext.getBean(beanName);
            Field[] fields = bean.getClass().getDeclaredFields();
            try{
                for (Field field: fields){
                    RpcAutowired rpcAutowired = field.getAnnotation(RpcAutowired.class);
                    if (rpcAutowired!=null){
                        String version = rpcAutowired.version();
                        field.setAccessible(true);
                        field.set(bean, createProxy(field.getType(), version));
                    }
                }
            } catch (IllegalAccessException ex){
                logger.error(ex.toString());
            }
        }
    }

    /**
     * 创建代理
     * @param interfaceClass 接口Class
     * @param version 服务版本号
     * @return 代理对象
     */
    /*jdk动态代理*/
    public <T> T createProxy(Class<T> interfaceClass, String version) {
        return (T)Proxy.newProxyInstance(interfaceClass.getClassLoader(),new Class<?>[]{interfaceClass},
                new RpcProxy<T>(interfaceClass, version, iConsumer,iLoadBalance, iNameService));
    }
}
