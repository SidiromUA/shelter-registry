package ua.edu.ldubgd.shelters.web;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import ua.edu.ldubgd.shelters.dto.CreateShelterRequest;
import ua.edu.ldubgd.shelters.exception.ValidationException;
import ua.edu.ldubgd.shelters.service.ShelterService;

import java.io.IOException;
import java.util.List;

/**
 * GET  /shelters — список укриттів і форма додавання (JSP-представлення);
 * POST /shelters — обробка форми додавання (доступно лише після входу, див. AuthFilter).
 */
@WebServlet("/shelters")
public class ShelterServlet extends HttpServlet {

    private ShelterService service;

    @Override
    public void init() {
        // Викликається контейнером один раз — при першому зверненні до сервлета
        service = (ShelterService) getServletContext().getAttribute(AppContextListener.SHELTER_SERVICE);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // «Flash»-повідомлення: показати один раз після перенаправлення
        HttpSession session = req.getSession(false);
        if (session != null && session.getAttribute("flash") != null) {
            req.setAttribute("message", session.getAttribute("flash"));
            session.removeAttribute("flash");
        }
        render(req, resp, List.of());
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        CreateShelterRequest form = new CreateShelterRequest(
                req.getParameter("name"),
                req.getParameter("address"),
                parseInt(req.getParameter("districtId")),
                req.getParameter("type"),
                parseInt(req.getParameter("capacity")),
                "true".equals(req.getParameter("accessible")));
        try {
            var created = service.create(form);
            req.getSession().setAttribute("flash", "Укриття «" + created.name() + "» додано");
            // Post/Redirect/Get: після успішного POST перенаправляємо, щоб F5 не надіслав форму вдруге
            resp.sendRedirect(req.getContextPath() + "/shelters");
        } catch (ValidationException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            req.setAttribute("form", form);
            render(req, resp, e.getErrors());
        }
    }

    private void render(HttpServletRequest req, HttpServletResponse resp, List<String> errors)
            throws ServletException, IOException {
        req.setAttribute("shelters", service.findAll());
        req.setAttribute("districts", service.findAllDistricts());
        req.setAttribute("errors", errors);
        req.getRequestDispatcher("/WEB-INF/views/shelters.jsp").forward(req, resp);
    }

    private static Integer parseInt(String value) {
        try {
            return value == null || value.isBlank() ? null : Integer.valueOf(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
