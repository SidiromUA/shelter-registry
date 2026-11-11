package ua.edu.ldubgd.shelters.web;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import ua.edu.ldubgd.shelters.model.User;
import ua.edu.ldubgd.shelters.service.AuthService;

import java.io.IOException;
import java.util.Optional;

/** GET /login — форма входу; POST /login — перевірка логіна й пароля, запис користувача в сесію. */
@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    public static final String USER = "user";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        AuthService auth = (AuthService) getServletContext().getAttribute(AppContextListener.AUTH_SERVICE);
        Optional<User> user = auth.authenticate(req.getParameter("username"), req.getParameter("password"));

        if (user.isEmpty()) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            req.setAttribute("error", "Неправильний логін або пароль");
            req.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(req, resp);
            return;
        }

        // Захист від фіксації сесії: після входу видаємо новий ідентифікатор сесії
        HttpSession old = req.getSession(false);
        if (old != null) {
            old.invalidate();
        }
        HttpSession session = req.getSession(true);
        session.setAttribute(USER, user.get());
        session.setMaxInactiveInterval(30 * 60);              // 30 хв бездіяльності

        resp.sendRedirect(req.getContextPath() + "/shelters");
    }
}
