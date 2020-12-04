package group.zeus.spi.communication;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

/**
 * @Author: maodazhan
 * @Date: 2020/11/19 19:51
 */
public class DispatcherServlet extends HttpServlet {

    private HttpServerHandler serverHandler;
    private Map<String, Object> handleMap;

    public DispatcherServlet(HttpServerHandler serverHandler, Map<String, Object> handleMap) {
        this.serverHandler = serverHandler;
        this.handleMap = handleMap;
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.serverHandler.handle(req, resp, handleMap);
    }
}
