package group.zeus.rpc.core;

import group.zeus.rpc.dto.RpcResponse;
import group.zeus.rpc.util.ServiceUtils;
import group.zeus.rpc.dto.RpcProtocol;
import group.zeus.rpc.dto.RpcRequest;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.sql.Driver;
import java.util.List;
import java.util.UUID;

/**
 * @Author: maodazhan
 * @Date: 2020/10/29 21:52
 */
public class RpcProxy<T> implements InvocationHandler {

    private Class<T> interfaceClass;
    private String version;
    private IConsumer iConsumer;
    private ILoadBalance iLoadBalance;
    private INameService iNameService;

    public RpcProxy(Class<T> interfaceClass, String version, IConsumer iConsumer, ILoadBalance iLoadBalance, INameService iNameService) {
        this.interfaceClass = interfaceClass;
        this.version = version;
        this.iConsumer = iConsumer;
        this.iLoadBalance = iLoadBalance;
        this.iNameService = iNameService;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        if (Object.class == method.getDeclaringClass()) {
            String name = method.getName();
            if ("equals".equals(name)) {
                return proxy == args[0];
            } else if ("hashCode".equals(name)) {
                return System.identityHashCode(proxy);
            } else if ("toString".equals(name)) {
                return proxy.getClass().getName() + "@" +
                        Integer.toHexString(System.identityHashCode(proxy)) +
                        ", with InvocationHandler " + this;
            } else {
                throw new IllegalStateException(String.valueOf(method));
            }
        }
        // 构造请求体
        RpcRequest rpcRequest = new RpcRequest();
        rpcRequest.setRequestId(UUID.randomUUID().toString());
        rpcRequest.setClassName(method.getDeclaringClass().getName());
        rpcRequest.setMethodName(method.getName());
        rpcRequest.setParameterTypes(method.getParameterTypes());
        rpcRequest.setParameters(args);
        rpcRequest.setVersion(version);
        String serviceKey = ServiceUtils.buildServiceKey(this.interfaceClass.getName(), version);
        RpcProtocol rpcProtocol = chooseRpcProtocol(serviceKey);
        Object result =  iConsumer.connect(rpcProtocol, rpcRequest);
        if (result instanceof RpcFuture)
            // 异步调用
            return ((RpcFuture) result).get();
        else if (result instanceof RpcResponse)
            // 同步调用
            return ((RpcResponse)result).getResult();
        else
            throw new RuntimeException("Wrong response object received,Check again...");
    }

    /**
     * 调用负载均衡的route方法
     * @param serviceKey 接口名#版本号
     * @return 选择后的服务地址
     */
    private RpcProtocol chooseRpcProtocol(String serviceKey){
        List<RpcProtocol> rpcProtocols = ServiceUtils.getProtocolsByServiceKey(serviceKey, iNameService.getNewestProtocols());
        if (rpcProtocols.size() == 0){
            throw new IllegalStateException("No available Service,check again");
        }
        return iLoadBalance.route(serviceKey, rpcProtocols);
    }
}
