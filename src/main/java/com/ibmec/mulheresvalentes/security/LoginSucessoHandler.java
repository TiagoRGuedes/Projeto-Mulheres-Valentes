package com.ibmec.mulheresvalentes.security;

import com.ibmec.mulheresvalentes.service.UsuarioService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/** Depois do login: registra o último acesso e leva ao painel (ou à página que a pessoa tentou abrir). */
@Component
public class LoginSucessoHandler extends SavedRequestAwareAuthenticationSuccessHandler {

    private final UsuarioService usuarioService;

    public LoginSucessoHandler(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
        setDefaultTargetUrl("/painel");
    }

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws ServletException, IOException {
        if (authentication.getPrincipal() instanceof UsuarioAutenticado usuario) {
            usuarioService.registrarAcesso(usuario.getId());
        }
        super.onAuthenticationSuccess(request, response, authentication);
    }
}
