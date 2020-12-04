package group.zeus.spi.communication;

import group.zeus.rpc.dto.RpcRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ObjectInputStream;

/**
 * @Author: maodazhan
 * @Date: 2020/11/19 19:30
 */
public class ProviderHttpHandler {

    private static final Logger logger = LoggerFactory.getLogger(ProviderHttpHandler.class);

    void handle(HttpServletRequest req, HttpServletResponse resp){
        try {
            ServletInputStream inputStream = req.getInputStream();
            ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);
            RpcRequest request = (RpcRequest) objectInputStream.readObject();

        } catch (Exception ex){
            logger.warn("Handle request  error: ", ex.getMessage());
        }

    }


}
