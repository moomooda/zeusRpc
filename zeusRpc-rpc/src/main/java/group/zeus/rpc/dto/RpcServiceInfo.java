package group.zeus.rpc.dto;

import com.alibaba.fastjson.JSON;

import java.io.Serializable;
import java.util.Objects;

/**
 * @Author: maodazhan
 * @Date: 2020/10/30 19:48
 */
public class RpcServiceInfo implements Serializable {

    private static final long serialVersionUID = -3813140784188140341L;

    private String serviceName;
    private String version;

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RpcServiceInfo that = (RpcServiceInfo) o;
        return Objects.equals(serviceName, that.serviceName) &&
                Objects.equals(version, that.version);
    }

    @Override
    public int hashCode() {
        return Objects.hash(serviceName, version);
    }

    /*fastjson*/
    private String toJson(){
        return JSON.toJSONString(this);
    }

    @Override
    public String toString() {
        return toJson();
    }
}
