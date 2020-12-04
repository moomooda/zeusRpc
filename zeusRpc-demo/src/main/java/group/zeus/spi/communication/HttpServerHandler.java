package group.zeus.spi.communication;

import group.zeus.rpc.dto.RpcRequest;
import group.zeus.rpc.dto.RpcResponse;
import group.zeus.rpc.util.ServiceUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cglib.reflect.FastClass;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Map;

/**
 * @Author: maodazhan
 * @Date: 2020/11/19 19:30
 */
public class HttpServerHandler {

    private static final Logger logger = LoggerFactory.getLogger(HttpServerHandler.class);

    void handle(HttpServletRequest req, HttpServletResponse resp, Map<String, Object> handleMap) {
        try {
            ServletInputStream inputStream = req.getInputStream();
            ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);
            // 取出RpcRequest
            RpcRequest request = (RpcRequest) objectInputStream.readObject();

            String className = request.getClassName();
            String version = request.getVersion();
            String serviceKey = ServiceUtils.buildServiceKey(className, version);
            Object serviceBean = handleMap.get(serviceKey);
            if (serviceBean == null) {
                logger.error("Cannot find service implementation with interface name and version: {}", className, version);
                return;
            }
            Class<?> serviceClass = serviceBean.getClass();
            String methodName = request.getMethodName();
            Class<?>[] parameterTypes = request.getParameterTypes();
            Object[] parameters = request.getParameters();

            RpcResponse response = new RpcResponse();
            Object result;
            try {
                // CGLIB reflect
                FastClass fastClass = FastClass.create(serviceClass);
                int methodIndex = fastClass.getIndex(methodName, parameterTypes);
                result = fastClass.invoke(methodIndex, serviceBean, parameters);            // 写入RpcResponse
                response.setRequestId(request.getRequestId());
                response.setResult(result);
            } catch (Exception ex) {
                response.setError(ex.getMessage());
                logger.warn("Handle request error: ", ex.getMessage());
            }
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(resp.getOutputStream());
            objectOutputStream.writeObject(response);
            objectOutputStream.flush();
            objectOutputStream.close();
        } catch (Exception ex) {
            //
        }
    }
}
