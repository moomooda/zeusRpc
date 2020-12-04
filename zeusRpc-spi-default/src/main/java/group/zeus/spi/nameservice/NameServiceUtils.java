package group.zeus.spi.nameservice;

import group.zeus.rpc.annotation.RpcService;
import group.zeus.rpc.dto.RpcProtocol;
import group.zeus.rpc.dto.RpcServiceInfo;
import group.zeus.rpc.util.ServiceUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @Author: maodazhan
 * @Date: 2020/11/21 13:50
 */
public class NameServiceUtils {

    private static final Logger logger = LoggerFactory.getLogger(NameServiceUtils.class);

    public static List<RpcServiceInfo> getRpcServiceInfos(Map<String, Object> serviceMap){
        List<RpcServiceInfo> serviceInfoList = new ArrayList<>();
        for (String key : serviceMap.keySet()) {
            String[] serviceInfo = key.split(ServiceUtils.SERVICE_CONCAT_TOKEN);
            if (serviceInfo.length > 0) {
                RpcServiceInfo rpcServiceInfo = new RpcServiceInfo();
                rpcServiceInfo.setServiceName(serviceInfo[0]);
                if (serviceInfo.length == 2) {
                    rpcServiceInfo.setVersion(serviceInfo[1]);
                } else {
                    rpcServiceInfo.setVersion("");
                }
                logger.info("Register new service: {} ", key);
                serviceInfoList.add(rpcServiceInfo);
            } else {
                logger.warn("Cannot get service name and version: {} ", key);
            }
        }
        return serviceInfoList;
    }

    public static RpcProtocol getRpcProtocol(String serviceAddress, List<RpcServiceInfo> serviceInfoList){
        String[] array = serviceAddress.split(":");
        String host = array[0];
        int port = Integer.parseInt(array[1]);
        RpcProtocol rpcProtocol = new RpcProtocol();
        rpcProtocol.setHost(host);
        rpcProtocol.setPort(port);
        rpcProtocol.setServiceInfoList(serviceInfoList);
        return rpcProtocol;
    }
}
