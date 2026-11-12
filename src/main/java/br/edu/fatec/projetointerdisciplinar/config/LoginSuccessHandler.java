package br.edu.fatec.projetointerdisciplinar.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import java.io.IOException;

public class LoginSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        boolean professor = hasRole(authentication, "ROLE_PROFESSOR");
        boolean responsavel = hasRole(authentication, "ROLE_RESPONSAVEL");

        if (hasRole(authentication, "ROLE_ADMIN")) {
            response.sendRedirect(request.getContextPath() + "/html/dashboard_admin.html");
            return;
        }

        String perfilSelecionado = request.getParameter("perfil");
        if ("PROFESSOR".equals(perfilSelecionado) && professor) {
            response.sendRedirect(request.getContextPath() + "/html/dashboard_professor.html");
        } else if ("RESPONSAVEL".equals(perfilSelecionado) && responsavel) {
            response.sendRedirect(request.getContextPath() + "/html/dashboard_responsavel.html");
        } else {
            SecurityContextHolder.clearContext();
            HttpSession session = request.getSession(false);
            if (session != null) {
                session.invalidate();
            }
            response.sendRedirect(request.getContextPath() + "/html/login.html?error&perfil="
                    + switch (perfilSelecionado == null ? "" : perfilSelecionado) {
                        case "PROFESSOR" -> "PROFESSOR";
                        default -> "RESPONSAVEL";
                    });
        }
    }

    private boolean hasRole(Authentication authentication, String role) {
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(role::equals);
    }
}
