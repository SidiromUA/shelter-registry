package ua.edu.ldubgd.shelters;

import org.apache.catalina.Context;
import org.apache.catalina.WebResourceRoot;
import org.apache.catalina.startup.Tomcat;
import org.apache.catalina.webresources.DirResourceSet;
import org.apache.catalina.webresources.StandardRoot;

import java.io.File;

/**
 * Запуск застосунку на вбудованому Tomcat без окремої інсталяції сервера:
 * mvn compile exec:exec  →  http://localhost:8080/shelters
 *
 * Для розгортання на «справжньому» Tomcat: mvn package і скопіювати target/shelters.war у tomcat/webapps.
 */
public class DevServer {

    public static void main(String[] args) throws Exception {
        int port = Integer.parseInt(System.getProperty("port", "8080"));

        Tomcat tomcat = new Tomcat();
        tomcat.setPort(port);
        tomcat.setBaseDir(new File("target/tomcat").getAbsolutePath());
        tomcat.getConnector();                                   // створює HTTP-конектор

        Context ctx = tomcat.addWebapp("", new File("src/main/webapp").getAbsolutePath());
        // Підключаємо скомпільовані класи, щоб Tomcat знайшов @WebServlet, @WebFilter, @WebListener
        WebResourceRoot resources = new StandardRoot(ctx);
        resources.addPreResources(new DirResourceSet(resources, "/WEB-INF/classes",
                new File("target/classes").getAbsolutePath(), "/"));
        ctx.setResources(resources);

        tomcat.start();
        System.out.println("Застосунок запущено: http://localhost:" + port + "/shelters");
        tomcat.getServer().await();
    }
}
