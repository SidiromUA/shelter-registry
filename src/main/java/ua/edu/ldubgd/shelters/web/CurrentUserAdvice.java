package ua.edu.ldubgd.shelters.web;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/** Додає в модель кожної сторінки відомості про поточного користувача (для меню і кнопок). */
@ControllerAdvice
public class CurrentUserAdvice {

    @ModelAttribute("currentUser")
    public String currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth == null || auth instanceof AnonymousAuthenticationToken ? null : auth.getName();
    }

    @ModelAttribute("isAdmin")
    public boolean isAdmin() {
        return hasRole("ROLE_ADMIN");
    }

    @ModelAttribute("canInspect")
    public boolean canInspect() {
        return hasRole("ROLE_ADMIN") || hasRole("ROLE_INSPECTOR");
    }

    private static boolean hasRole(String role) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.getAuthorities().stream().map(GrantedAuthority::getAuthority).anyMatch(role::equals);
    }
}
