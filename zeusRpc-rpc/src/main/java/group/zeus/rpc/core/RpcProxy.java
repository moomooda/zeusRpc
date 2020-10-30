package group.zeus.rpc.core;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * @Author: maodazhan
 * @Date: 2020/10/29 21:52
 */
public class RpcProxy<T> implements InvocationHandler {

    private Class<?> interfaceClass;
    private String version;

    public RpcProxy(Class<?> interfaceClass, String version) {
        this.interfaceClass = interfaceClass;
        this.version = version;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        return null;
    }
}
