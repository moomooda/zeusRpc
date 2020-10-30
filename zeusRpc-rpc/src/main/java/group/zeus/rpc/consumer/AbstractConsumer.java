package group.zeus.rpc.consumer;


import group.zeus.rpc.core.RpcProxy;

import java.lang.reflect.Proxy;

/**
 * @Author: maodazhan
 * @Date: 2020/10/29 21:30
 */
public abstract class AbstractConsumer implements IConsumer{

    @Override
    public <T> T createProxy(Class<T> interfaceClass, String version) {
        return (T) Proxy.newProxyInstance(interfaceClass.getClassLoader(),new Class<?>[]{interfaceClass},
                new RpcProxy<T>(interfaceClass, version));
    }



}
