package group.zeus.rpc.provider;


import group.zeus.rpc.util.ServiceUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author: maodazhan
 * @Date: 2020/10/28 20:26
 */
public abstract class AbstractProvider implements IProvider{

    private static final Logger Logger = LoggerFactory.getLogger(AbstractProvider.class);
    private Map<String, Object> serviceMap = new HashMap<>();

    @Override
    public void addService(String interfaceName, String version, Object serviceBean) {
        Logger.info("Adding service, interface: {}, version: {}, bean：{}", interfaceName, version, serviceBean);
        String serviceKey = ServiceUtils.buildServiceKey(interfaceName, version);
        serviceMap.put(serviceKey, serviceBean);
    }
}
