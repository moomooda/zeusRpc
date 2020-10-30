package group.zeus.rpc.util;

import group.zeus.rpc.core.RpcProtocal;
import group.zeus.rpc.core.RpcRequestHanlder;
import group.zeus.rpc.core.RpcServiceInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author: maodazhan
 * @Date: 2020/10/28 20:35
 */
public class ServiceUtils {

    private static final String SERVICE_CONCAT_TOKEN ="#";

    /*构建服务的key*/
    public static String buildServiceKey(String interfaceName, String version){
        String serviceKey = interfaceName;
        if (version!=null&& version.trim().length()>0){
            serviceKey += SERVICE_CONCAT_TOKEN.concat(version);
        }
        return serviceKey;
    }

    /*用于负载均衡*/
    public static Map<String, List<RpcProtocal>> getConnGroupByServiceKey(Map<RpcProtocal, RpcRequestHanlder> Connect_Nodes){
        Map<String, List<RpcProtocal>> serviceMap = new HashMap<>();
        if (Connect_Nodes !=null && Connect_Nodes.size() > 0){
            // 遍历每一个连接地址
            for (RpcProtocal rpcProtocal: Connect_Nodes.keySet()){
                // 遍历服务地址所有服务
                for(RpcServiceInfo rpcServiceInfo : rpcProtocal.getServiceInfoList()){
                    String serviceKey = ServiceUtils.buildServiceKey(rpcServiceInfo.getServiceName(), rpcServiceInfo.getVersion());
                    List<RpcProtocal> rpcProtocalList  = serviceMap.get(serviceKey);
                    if (rpcProtocalList ==null)
                        rpcProtocalList = new ArrayList<>();
                    rpcProtocalList.add(rpcProtocal);
                    serviceMap.putIfAbsent(serviceKey, rpcProtocalList);
                }
            }
        }
        return serviceMap;
    }

}
