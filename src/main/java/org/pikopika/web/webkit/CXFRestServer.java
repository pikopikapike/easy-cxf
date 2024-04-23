package org.pikopika.web.webkit;

import com.fasterxml.jackson.jakarta.rs.json.JacksonJsonProvider;
import easysocket.utils.ClassHelper;
import org.apache.cxf.endpoint.Server;
import org.apache.cxf.jaxrs.JAXRSServerFactoryBean;
import org.apache.cxf.jaxrs.interceptor.JAXRSOutInterceptor;
import org.apache.cxf.jaxrs.openapi.OpenApiFeature;
import org.apache.cxf.logging.FaultListener;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.rs.security.cors.CrossOriginResourceSharingFilter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

public class CXFRestServer {

    static Logger logger = LogManager.getLogger(CXFRestServer.class);

    private final int port;

    private Server server;
    private final String[] servicePackages;

    private AbstractPhaseInterceptor<Message>[] interceptors;

    public CXFRestServer(int port, String... servicePackages) {
        this.port = port;
        this.servicePackages = servicePackages;

    }

    public void setInterceptors(AbstractPhaseInterceptor<Message>... interceptors) {
        this.interceptors = interceptors;
    }


    /**
     * {@inheritDoc}
     */
    public void start() {
        this.start(null);
    }

    /**
     * <p>Start new Apache CXF {@link Server}</p>
     *
     * <p>
     * ThreadPoolTaskExecutor configurations <br>
     * <b>corePoolSize</b>: default value is 4. {@link ThreadPoolTaskExecutor}<br>
     * <b>maxPoolSize</b>: default value is 20. {@link ThreadPoolTaskExecutor}<br>
     * <b>maxQueueSize</b>: default value is 10000. {@link ThreadPoolTaskExecutor}<br>
     * <b>keepAliveSeconds</b>: default value is 60. {@link ThreadPoolTaskExecutor}<br>
     * </p>
     *
     * @param props {@code ThreadPoolTaskExecutor} configuration
     * @see ThreadPoolTaskExecutor
     */
    public void start(Properties props) {
        this.server = this.initServer(props);
        logger.debug("server start at port:{}", port);
    }

    public void join() throws InterruptedException {
        Thread.currentThread().join();
    }

    private Server initServer(Properties props) {

        List<Class<?>> serviceObjects = new ArrayList<>();
        for (String packageName : this.servicePackages) {
            Set<Class<?>> classSet = ClassHelper.getClasses(packageName);
            for (Class<?> clazz : classSet) {
                if (clazz.isAnnotationPresent(RestController.class)) {
                    serviceObjects.add(clazz);

                    logger.debug("rest api service class detected => {}", clazz.getName());
                }
            }
        }


        JAXRSServerFactoryBean factory = new JAXRSServerFactoryBean();
        factory.setResourceClasses(serviceObjects);

        // FIXME 접속 주소 http://localhost:7001/api/api-docs?url=/api/openapi.json#/
        OpenApiFeature openApiFeature = new OpenApiFeature();
        factory.getFeatures().add(openApiFeature);

//        Swagger2Feature swagger = new Swagger2Feature();
////        swagger.setBasePath("/api/");
//        swagger.setUsePathBasedConfig(true);
//        factory.getFeatures().add(swagger);

        factory.getBus().setProperty(FaultListener.class.getName(), new CxfFaultListenerImpl());

        factory.setAddress("http://0.0.0.0:" + port + "/");
        factory.setProviders(Arrays.asList(
//				new JsrJsonpProvider(),
                new JacksonJsonProvider(),
                new AssertExceptionMapper(),
                new CrossOriginResourceSharingFilter()
        ));
        factory.getOutInterceptors().add(new JAXRSOutInterceptor());
        if (this.interceptors != null) {
            for (var interceptor : this.interceptors) {
                factory.getInInterceptors().add(interceptor);
            }
        }



        int corePoolSize = 4;
        int maxPoolSize = 20;
        int queueCapacity = 10000;
        int keepAliveSeconds = 60;

        if (props != null) {
            if (props.containsKey("corePoolSize"))
                corePoolSize = Integer.parseInt(props.getProperty("corePoolSize"));
            if (props.containsKey("maxPoolSize"))
                maxPoolSize = Integer.parseInt(props.getProperty("maxPoolSize"));
            if (props.containsKey("queueCapacity"))
                queueCapacity = Integer.parseInt(props.getProperty("queueCapacity"));
            if (props.containsKey("keepAliveSeconds"))
                keepAliveSeconds = Integer.parseInt(props.getProperty("keepAliveSeconds"));
        }

        ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
        threadPoolTaskExecutor.setThreadNamePrefix("CXF_THREAD_POOL_");
        threadPoolTaskExecutor.setCorePoolSize(corePoolSize);
        threadPoolTaskExecutor.setMaxPoolSize(maxPoolSize);
        threadPoolTaskExecutor.setQueueCapacity(queueCapacity);
        threadPoolTaskExecutor.setKeepAliveSeconds(keepAliveSeconds);
        threadPoolTaskExecutor.initialize();

        factory.getServiceFactory().setExecutor(threadPoolTaskExecutor);

        Server server = factory.create();


        return server;
    }
}
