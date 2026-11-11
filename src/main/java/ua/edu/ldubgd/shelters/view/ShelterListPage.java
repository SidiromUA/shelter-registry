package ua.edu.ldubgd.shelters.view;

import ua.edu.ldubgd.shelters.dto.ShelterView;

import java.util.List;

/**
 * Представлення (V у MVC): формує HTML-сторінку з урахуванням вимог доступності WCAG 2.2:
 * мова документа, семантичні орієнтири, заголовки таблиці зі scope, caption,
 * текстові (а не лише кольорові) позначення стану, оголошення повідомлень для екранних читачів.
 */
public class ShelterListPage {

    public String render(List<ShelterView> shelters, List<String> errors, String message) {
        StringBuilder html = new StringBuilder();
        html.append("""
                <!DOCTYPE html>
                <html lang="uk">
                <head>
                  <meta charset="UTF-8">
                  <meta name="viewport" content="width=device-width, initial-scale=1">
                  <title>Укриття — Реєстр укриттів</title>
                  <link rel="stylesheet" href="css/style.css">
                </head>
                <body>
                  <a class="skip-link" href="#main">Перейти до основного вмісту</a>
                  <header class="site-header"><p class="logo">Реєстр укриттів</p></header>
                  <main id="main">
                    <h1>Укриття міста</h1>
                """);

        if (message != null) {
            html.append("<p class=\"message\" role=\"status\">").append(escape(message)).append("</p>\n");
        }
        if (!errors.isEmpty()) {
            html.append("<div class=\"errors\" role=\"alert\"><h2>Виправте помилки</h2><ul>\n");
            errors.forEach(e -> html.append("<li>").append(escape(e)).append("</li>\n"));
            html.append("</ul></div>\n");
        }

        html.append("""
                <table>
                  <caption>Зареєстровано укриттів: %d</caption>
                  <thead><tr>
                    <th scope="col">Назва</th><th scope="col">Адреса</th><th scope="col">Район</th>
                    <th scope="col">Тип</th><th scope="col">Місткість, осіб</th>
                    <th scope="col">Доступність</th><th scope="col">Стан</th>
                  </tr></thead>
                  <tbody>
                """.formatted(shelters.size()));
        for (ShelterView s : shelters) {
            html.append("<tr>")
                    .append(td(s.name())).append(td(s.address())).append(td(s.district()))
                    .append(td(s.type())).append(td(String.valueOf(s.capacity())))
                    .append(td(s.accessibility()))
                    .append("<td><span class=\"status status-")
                    .append(s.statusCode().toLowerCase().replace('_', '-')).append("\">")
                    .append(escape(s.status())).append("</span></td>")
                    .append("</tr>\n");
        }
        html.append("""
                  </tbody>
                </table>
                  </main>
                </body>
                </html>
                """);
        return html.toString();
    }

    private static String td(String value) {
        return "<td>" + escape(value) + "</td>";
    }

    /** Екранування HTML — захист від XSS, коли дані потрапляють на сторінку. */
    static String escape(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;").replace("'", "&#39;");
    }
}
