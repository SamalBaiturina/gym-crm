package com.epam.gymcrm;

import com.epam.gymcrm.infrastructure.config.AppConfig;
import com.epam.gymcrm.infrastructure.web.WebConfig;
import org.apache.catalina.Context;
import org.apache.catalina.Wrapper;
import org.apache.catalina.startup.Tomcat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Starts the REST API on an embedded Tomcat. The port is 8080 unless -Dserver.port is given.
 * Needs PostgreSQL, like {@link Main}.
 */
public class RestApplication {

    private static final Logger log = LoggerFactory.getLogger(RestApplication.class);

    public static void main(String[] args) throws Exception {
        int port = Integer.getInteger("server.port", 8080);

        AnnotationConfigWebApplicationContext context = new AnnotationConfigWebApplicationContext();
        context.register(AppConfig.class, WebConfig.class);

        Path baseDir = Files.createTempDirectory("gymcrm-tomcat");
        Tomcat tomcat = new Tomcat();
        tomcat.setBaseDir(baseDir.toString());
        tomcat.setPort(port);
        tomcat.getConnector();

        Context tomcatContext = tomcat.addContext("", baseDir.toString());
        Wrapper dispatcher = Tomcat.addServlet(tomcatContext, "dispatcher", new DispatcherServlet(context));
        dispatcher.setLoadOnStartup(1);
        tomcatContext.addServletMappingDecoded("/", "dispatcher");

        tomcat.start();
        log.info("Gym CRM REST API started on port {}", port);
        tomcat.getServer().await();
    }
}
