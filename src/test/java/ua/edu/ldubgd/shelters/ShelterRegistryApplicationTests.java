package ua.edu.ldubgd.shelters;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.hasItem;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Інтеграційні тести: увесь застосунок (усі шари + H2 + Flyway + Spring Security).
 * @Transactional — після кожного тесту зміни в БД відкочуються.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ShelterRegistryApplicationTests {

    @Autowired
    MockMvc mvc;

    @Test
    void loginWithBcryptPassword() throws Exception {
        mvc.perform(formLogin().user("admin").password("admin")).andExpect(authenticated().withRoles("ADMIN"));
        mvc.perform(formLogin().user("admin").password("wrong")).andExpect(unauthenticated());
    }

    @Test
    void inspectorRegistersInspection() throws Exception {
        mvc.perform(post("/api/shelters/4/inspections").with(httpBasic("inspector", "inspector"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"result\":\"PASSED\",\"notes\":\"Недоліки усунено\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value("READY"));

        mvc.perform(get("/api/shelters/4/inspections"))
                .andExpect(jsonPath("$[*].inspector", hasItem("Петренко Олена")));
    }

    @Test
    void inspectorCannotDelete() throws Exception {
        mvc.perform(delete("/api/shelters/1").with(httpBasic("inspector", "inspector")))
                .andExpect(status().isForbidden());
        mvc.perform(delete("/api/shelters/1").with(httpBasic("admin", "admin")))
                .andExpect(status().isNoContent());
    }

    @Test
    void publicPagesAndProtectedForms() throws Exception {
        mvc.perform(get("/shelters")).andExpect(status().isOk());
        mvc.perform(get("/api/shelters?page=0&size=2&sort=capacity,desc"))
                .andExpect(jsonPath("$.totalElements").value(6))
                .andExpect(jsonPath("$.content[0].capacity").value(450));
        // форма додавання вимагає входу — перенаправлення на /login
        mvc.perform(get("/shelters/new")).andExpect(status().is3xxRedirection());
    }
}
