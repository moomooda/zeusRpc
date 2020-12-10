package group.zeus.spi.communication;

import group.zeus.rpc.core.IConsumer;
import group.zeus.rpc.dto.RpcProtocol;
import group.zeus.rpc.dto.RpcRequest;
import group.zeus.rpc.dto.RpcResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * @Author: maodazhan
 * @Date: 2020/11/17 22:20
 */
public class TomcatConsumerClient implements IConsumer {

    private static final Logger logger = LoggerFactory.getLogger(TomcatConsumerClient.class);

    @Override
    public Object connect(RpcProtocol rpcProtocol, RpcRequest request) throws Exception {

        String host = rpcProtocol.getHost();
        int port = rpcProtocol.getPort();
        URL url = new URL("http", host, port, "/");
        try {
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setRequestMethod("POST");
            // 可读可写
            httpURLConnection.setDoInput(true);
            httpURLConnection.setDoOutput(true);

            OutputStream outputStream = httpURLConnection.getOutputStream();
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
            objectOutputStream.writeObject(request);
            objectOutputStream.flush();
            objectOutputStream.close();

            InputStream inputStream = httpURLConnection.getInputStream();
            ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);
            RpcResponse result = (RpcResponse) objectInputStream.readObject();
            objectInputStream.close();

            return result;
        } catch(Exception ex){
            logger.error("Request error: ", ex.getMessage());
        }
        return null;
    }

    @Override
    public void stop() throws Exception {
        // pass
    }
}
