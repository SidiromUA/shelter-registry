package ua.edu.ldubgd.shelters.web;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import ua.edu.ldubgd.shelters.exception.NotFoundException;
import ua.edu.ldubgd.shelters.model.User;
import ua.edu.ldubgd.shelters.service.ShelterService;

import java.io.IOException;

/** POST /shelters/delete?id=… — видалення укриття (лише для ролі ADMIN). */
@WebServlet("/shelters/delete")
public class ShelterDeleteServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        User user = (User) req.getSession().getAttribute(LoginServlet.USER);
        if (user.role() != User.Role.ADMIN) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Видаляти укриття може лише адміністратор");
            return;
        }
        ShelterService service =
                (ShelterService) getServletContext().getAttribute(AppContextListener.SHELTER_SERVICE);
        try {
            service.delete(Integer.parseInt(req.getParameter("id")));
            req.getSession().setAttribute("flash", "Укриття видалено");
        } catch (NumberFormatException | NotFoundException e) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Укриття не знайдено");
            return;
        }
        resp.sendRedirect(req.getContextPath() + "/shelters");
    }
}
