package group.zeus.rpc.core;

import java.io.Serializable;
import java.util.List;

/**
 * @Author: maodazhan
 * @Date: 2020/10/30 10:43
 */
public class RpcProtocal implements Serializable{

    private static final long serialVersionUID = 5475362632015832551L;
    // service host
    private String host;
    // service port
    private String port;
    // service info list
    private List<RpcServiceInfo> serviceInfoList;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public List<RpcServiceInfo> getServiceInfoList() {
        return serviceInfoList;
    }

    public void setServiceInfoList(List<RpcServiceInfo> serviceInfoList) {
        this.serviceInfoList = serviceInfoList;
    }
}
