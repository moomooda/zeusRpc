package group.zeus.rpc.util;

import group.zeus.rpc.dto.RpcProtocol;
import group.zeus.rpc.dto.RpcServiceInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @Author: maodazhan
 * @Date: 2020/10/28 20:35
 */
public class ServiceUtils {

    public static final String SERVICE_CONCAT_TOKEN = "#";

    private static final Logger Logger = LoggerFactory.getLogger(ServiceUtils.class);

    /*构建服务的key*/
    public static String buildServiceKey(String interfaceName, String version) {
        String serviceKey = interfaceName;
        if (version != null && version.trim().length() > 0) {
            serviceKey += SERVICE_CONCAT_TOKEN.concat(version);
        }
        return serviceKey;
    }

    /*rpcProtocols分组，只取当前serviceKey对应的*/
    public static List<RpcProtocol> getProtocolsByServiceKey(String serviceKey, List<RpcProtocol> rpcProtocols) {
        if (rpcProtocols == null || rpcProtocols.size() == 0) {
            throw new IllegalStateException("No available Service,check again");
        }
        List<RpcProtocol> results = new ArrayList<>();
        // 遍历每一个连接
        for (RpcProtocol rpcProtocol : rpcProtocols){
            // 遍历连接的每一项服务
            for (RpcServiceInfo rpcServiceInfo : rpcProtocol.getServiceInfoList()){
                String thatServiceKey = buildServiceKey(rpcServiceInfo.getServiceName(), rpcServiceInfo.getVersion());
                if (serviceKey.equals(thatServiceKey)){
                    results.add(rpcProtocol);
                }
            }
        }
        return results;
    }

    /*用于注册暴露服务的bean*/
    public static void addService(Map<String, Object> serviceMap, String interfaceName, String version, Object serviceBean) {
        Logger.info("Adding service, interface: {}, version: {}, bean：{}", interfaceName, version, serviceBean);
        String serviceKey = ServiceUtils.buildServiceKey(interfaceName, version);
        serviceMap.put(serviceKey, serviceBean);
    }
}
