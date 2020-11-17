package group.zeus.rpc.dto;

import com.alibaba.fastjson.JSON;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

/**
 * @Author: maodazhan
 * @Date: 2020/10/30 10:43
 */
public class RpcProtocol implements Serializable{

    private static final long serialVersionUID = 5475362632015832551L;
    // service host
    private String host;
    // service port
    private int port;
    // service info list
    private List<RpcServiceInfo> serviceInfoList;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public List<RpcServiceInfo> getServiceInfoList() {
        return serviceInfoList;
    }

    public void setServiceInfoList(List<RpcServiceInfo> serviceInfoList) {
        this.serviceInfoList = serviceInfoList;
    }

    public static RpcProtocol fromJson(String json){
        return JSON.parseObject(json, RpcProtocol.class);
    }

    public String toJson(){
        String json = JSON.toJSONString(this);
        return json;
    }

    @Override
    public int hashCode() {
        return Objects.hash(host, port, serviceInfoList);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RpcProtocol that = (RpcProtocol) o;
        return port == that.port &&
                Objects.equals(host, that.host) &&
                Objects.equals(serviceInfoList, that.serviceInfoList);
    }
}
