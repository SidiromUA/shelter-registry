package ua.edu.ldubgd.shelters.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import ua.edu.ldubgd.shelters.repository.UserRepository;

/**
 * Spring Security. Два ланцюжки фільтрів:
 *  1) REST API (/api/**) — HTTP Basic, без сесій і CSRF (клієнти — програми, Postman);
 *  2) вебсторінки — форма входу, сесія, захист CSRF (Thymeleaf додає токен у форми автоматично).
 * Ролі: ADMIN — усе; INSPECTOR — реєстрація перевірок; гість — лише перегляд.
 */
@Configuration
@EnableMethodSecurity                       // вмикає @PreAuthorize на методах сервісів
public class SecurityConfig {

    @Bean
    @Order(1)
    SecurityFilterChain apiSecurity(HttpSecurity http) throws Exception {
        http.securityMatcher("/api/**")
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.GET, "/api/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/shelters/*/inspections").hasAnyRole("INSPECTOR", "ADMIN")
                        .anyRequest().hasRole("ADMIN"))
                .httpBasic(Customizer.withDefaults())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .csrf(csrf -> csrf.disable());
        return http.build();
    }

    @Bean
    @Order(2)
    SecurityFilterChain webSecurity(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(auth -> auth
                        .requestMatchers("/css/**", "/login", "/error", "/h2-console/**",
                                "/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**",
                                "/actuator/health", "/actuator/info").permitAll()
                        // Порядок правил важливий: шаблон /shelters/{id} збігся б і з /shelters/new,
                        // тому сторінки форм для адміністратора перевіряються раніше
                        .requestMatchers("/shelters/new", "/shelters/*/edit").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/", "/shelters", "/shelters/{id}").permitAll()
                        .requestMatchers(HttpMethod.POST, "/shelters/*/inspections").hasAnyRole("INSPECTOR", "ADMIN")
                        .requestMatchers("/shelters/**").hasRole("ADMIN")
                        .anyRequest().authenticated())
                .formLogin(form -> form
                        .loginPage("/login")                 // власна сторінка входу (templates/login.html)
                        .defaultSuccessUrl("/shelters")
                        .permitAll())
                .logout(logout -> logout.logoutSuccessUrl("/shelters"))
                // H2-консоль (лише профіль dev) працює у фреймах і не надсилає CSRF-токен
                .csrf(csrf -> csrf.ignoringRequestMatchers("/h2-console/**"))
                .headers(h -> h.frameOptions(f -> f.sameOrigin()));
        return http.build();
    }

    /** Користувачі — з таблиці users через JPA. */
    @Bean
    UserDetailsService userDetailsService(UserRepository users) {
        return username -> users.findByUsername(username)
                .map(u -> org.springframework.security.core.userdetails.User
                        .withUsername(u.getUsername())
                        .password(u.getPasswordHash())
                        .roles(u.getRole().name())
                        .build())
                .orElseThrow(() -> new UsernameNotFoundException(username));
    }

    /** Розпізнає префікс {bcrypt} у хеші і перевіряє пароль відповідним алгоритмом. */
    @Bean
    PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }
}
