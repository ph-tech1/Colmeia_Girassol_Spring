package br.edu.fatec.projetointerdisciplinar.config;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LoginSuccessHandlerTest {

    private final LoginSuccessHandler handler = new LoginSuccessHandler();

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void rejectsResponsibleWhenProfessorModeWasSelected() throws Exception {
        MockHttpServletRequest request = request("PROFESSOR");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockHttpSession session = new MockHttpSession();
        request.setSession(session);
        Authentication authentication = authentication("ROLE_RESPONSAVEL");
        SecurityContextHolder.getContext().setAuthentication(authentication);

        handler.onAuthenticationSuccess(request, response, authentication);

        assertEquals("/app/html/login.html?error&perfil=PROFESSOR", response.getRedirectedUrl());
        assertTrue(session.isInvalid());
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void rejectsProfessorWhenResponsibleModeWasSelected() throws Exception {
        MockHttpServletRequest request = request("RESPONSAVEL");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockHttpSession session = new MockHttpSession();
        request.setSession(session);
        Authentication authentication = authentication("ROLE_PROFESSOR");
        SecurityContextHolder.getContext().setAuthentication(authentication);

        handler.onAuthenticationSuccess(request, response, authentication);

        assertEquals("/app/html/login.html?error&perfil=RESPONSAVEL", response.getRedirectedUrl());
        assertTrue(session.isInvalid());
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void redirectsToDashboardForMatchingProfile() throws Exception {
        MockHttpServletRequest request = request("PROFESSOR");
        MockHttpServletResponse response = new MockHttpServletResponse();

        handler.onAuthenticationSuccess(request, response, authentication("ROLE_PROFESSOR"));

        assertEquals("/app/html/dashboard_professor.html", response.getRedirectedUrl());
    }

    @Test
    void keepsAdminRedirectIndependentOfSelectedProfile() throws Exception {
        MockHttpServletRequest request = request("RESPONSAVEL");
        MockHttpServletResponse response = new MockHttpServletResponse();

        handler.onAuthenticationSuccess(request, response, authentication("ROLE_ADMIN"));

        assertEquals("/app/html/dashboard_admin.html", response.getRedirectedUrl());
    }

    private MockHttpServletRequest request(String perfil) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setContextPath("/app");
        request.setParameter("perfil", perfil);
        return request;
    }

    private Authentication authentication(String... roles) {
        return new UsernamePasswordAuthenticationToken("12345678901", "",
                Arrays.stream(roles).map(SimpleGrantedAuthority::new).toList());
    }

}
