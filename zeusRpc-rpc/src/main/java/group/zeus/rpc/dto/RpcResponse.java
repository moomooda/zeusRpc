package group.zeus.rpc.dto;

import java.io.Serializable;

/**
 * @Author: maodazhan
 * @Date: 2020/10/28 14:47
 */
public class RpcResponse implements Serializable {

    private static final long serialVersionUID = 5751586130678835808L;

    private String requestId;
    private String error;
    private Object result;

    public String getRequestId() {
        return requestId;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }
}
