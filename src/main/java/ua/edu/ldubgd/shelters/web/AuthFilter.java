package ua.edu.ldubgd.shelters.web;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

/**
 * Фільтр авторизації: перегляд списку відкритий для всіх,
 * а будь-які зміни (POST на /shelters…) — лише для користувачів, що увійшли.
 */
@WebFilter(urlPatterns = {"/shelters", "/shelters/*"})
public class AuthFilter extends HttpFilter {

    @Override
    protected void doFilter(HttpServletRequest req, HttpServletResponse resp, FilterChain chain)
            throws IOException, ServletException {
        boolean modifying = !"GET".equalsIgnoreCase(req.getMethod());
        HttpSession session = req.getSession(false);
        boolean loggedIn = session != null && session.getAttribute(LoginServlet.USER) != null;

        if (modifying && !loggedIn) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }
        chain.doFilter(req, resp);                            // передати запит далі — сервлету
    }
}
