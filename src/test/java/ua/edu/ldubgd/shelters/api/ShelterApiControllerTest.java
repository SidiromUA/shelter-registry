package ua.edu.ldubgd.shelters.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ua.edu.ldubgd.shelters.config.SecurityConfig;
import ua.edu.ldubgd.shelters.dto.ShelterView;
import ua.edu.ldubgd.shelters.exception.NotFoundException;
import ua.edu.ldubgd.shelters.repository.UserRepository;
import ua.edu.ldubgd.shelters.service.ShelterService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Тести вебшару: піднімається лише Spring MVC з одним контролером і налаштуваннями безпеки,
 * сервіс замінено на mock. MockMvc імітує HTTP-запити без запуску сервера.
 */
@WebMvcTest(ShelterApiController.class)
@Import(SecurityConfig.class)
class ShelterApiControllerTest {

    private static final String VALID_JSON = """
            {"name":"Нове укриття","address":"вул. Нова, 1","districtId":4,
             "type":"BASEMENT","capacity":70,"accessible":true}
            """;

    @Autowired MockMvc mvc;
    @MockitoBean ShelterService service;
    @MockitoBean UserRepository users;          // потрібен бінові UserDetailsService із SecurityConfig

    @Test
    void anyoneCanReadShelter() throws Exception {
        when(service.findById(1)).thenReturn(view(1));

        mvc.perform(get("/api/shelters/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Укриття № 1"))
                .andExpect(jsonPath("$.statusCode").value("READY"));
    }

    @Test
    void unknownShelterIs404ProblemDetail() throws Exception {
        when(service.findById(999)).thenThrow(new NotFoundException("Укриття з id=999 не знайдено"));

        mvc.perform(get("/api/shelters/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Не знайдено"));
    }

    @Test
    void anonymousCannotCreate() throws Exception {
        mvc.perform(post("/api/shelters").contentType(MediaType.APPLICATION_JSON).content(VALID_JSON))
                .andExpect(status().isUnauthorized());
        verify(service, never()).create(any());
    }

    @Test
    void inspectorCannotCreate() throws Exception {
        mvc.perform(post("/api/shelters").with(user("inspector").roles("INSPECTOR"))
                        .contentType(MediaType.APPLICATION_JSON).content(VALID_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    void invalidBodyIs400WithFieldErrors() throws Exception {
        mvc.perform(post("/api/shelters").with(user("admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"\",\"districtId\":4,\"type\":\"BASEMENT\",\"capacity\":0}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.name").value("Вкажіть назву укриття"))
                .andExpect(jsonPath("$.errors.address").exists())
                .andExpect(jsonPath("$.errors.capacity").exists());
        verify(service, never()).create(any());
    }

    @Test
    void adminCreatesShelter() throws Exception {
        when(service.create(any())).thenReturn(view(7));

        mvc.perform(post("/api/shelters").with(user("admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON).content(VALID_JSON))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/api/shelters/7"));
    }

    private static ShelterView view(int id) {
        return new ShelterView(id, "Укриття № " + id, "вул. Клепарівська, 35", "Шевченківський", "Підвал",
                120, "Так", "Готове", "READY", null);
    }
}
