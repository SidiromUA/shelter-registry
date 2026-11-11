package ua.edu.ldubgd.shelters.web;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import ua.edu.ldubgd.shelters.repository.jdbc.JdbcDistrictRepository;
import ua.edu.ldubgd.shelters.repository.jdbc.JdbcInspectionRepository;
import ua.edu.ldubgd.shelters.repository.jdbc.JdbcShelterRepository;
import ua.edu.ldubgd.shelters.repository.jdbc.JdbcUserRepository;
import ua.edu.ldubgd.shelters.service.AuthService;
import ua.edu.ldubgd.shelters.service.ShelterService;

/**
 * Слухач життєвого циклу вебзастосунку: під час запуску створює сервіси
 * і зберігає їх в атрибутах ServletContext, звідки їх беруть сервлети.
 */
@WebListener
public class AppContextListener implements ServletContextListener {

    public static final String SHELTER_SERVICE = "shelterService";
    public static final String AUTH_SERVICE = "authService";

    @Override
    public void contextInitialized(ServletContextEvent event) {
        ServletContext ctx = event.getServletContext();
        ctx.setAttribute(SHELTER_SERVICE, new ShelterService(
                new JdbcShelterRepository(), new JdbcDistrictRepository(), new JdbcInspectionRepository()));
        ctx.setAttribute(AUTH_SERVICE, new AuthService(new JdbcUserRepository()));
        ctx.log("Реєстр укриттів: сервіси ініціалізовано");
    }

    @Override
    public void contextDestroyed(ServletContextEvent event) {
        event.getServletContext().log("Реєстр укриттів: застосунок зупинено");
    }
}
