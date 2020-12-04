package group.zeus.spi.communication;

import group.zeus.rpc.core.IProvider;
import org.apache.catalina.Context;
import org.apache.catalina.Engine;
import org.apache.catalina.Host;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.Server;
import org.apache.catalina.Service;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.core.StandardEngine;
import org.apache.catalina.core.StandardHost;
import org.apache.catalina.startup.Tomcat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;


/**
 * @Author: maodazhan
 * @Date: 2020/11/17 22:17
 */
public class TomcatProviderServer implements IProvider {

    private static final Logger logger = LoggerFactory.getLogger(TomcatProviderServer.class);
    private Tomcat tomcat;
    private Thread thread;

    @Override
    public void start(String serviceAddress, Map<String, Object> handleMap) throws Exception {
        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                String[] addr = serviceAddress.split(":");
                String ip = addr[0];
                Integer port = Integer.parseInt(addr[1]);

                tomcat = new Tomcat();
                Server server = tomcat.getServer();
                // Tomcat是默认的service
                Service service = server.findService("Tomcat");
                // 默认使用 Http 1.1 NIO协议
                Connector connector = new Connector();
                connector.setPort(port);
                Engine engine = new StandardEngine();
                engine.setDefaultHost(ip);
                Host host = new StandardHost();
                host.setName(ip);
                String contextPath = "";
                Context context = new StandardContext();
                context.setPath(contextPath);
                context.addLifecycleListener(new Tomcat.FixContextListener());
                host.addChild(context);
                engine.addChild(host);
                service.setContainer(engine);
                service.addConnector(connector);

                tomcat.addServlet(contextPath, "dispatcher", new DispatcherServlet(new HttpServerHandler(), handleMap));
                context.addServletMappingDecoded("/*", "dispatcher");

                try {
                    tomcat.start();
                    server.await();
                } catch (Exception ex) {
                    logger.error("Tomcat Rpc server error", ex);
                }
                finally {
                    logger.info("Tomcat Rpc server stop");
                }
            }
        });
        thread.start();
    }

    @Override
    public void stop() throws Exception {
        //destroy server thread
        tomcat.stop();
        tomcat.destroy();
    }
}
