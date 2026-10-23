package com.ibmec.mulheresvalentes.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Deixa passar apenas a página pública, a inscrição e a própria tela de login.
 * Todo o resto do sistema exige a sessão criada em {@code /admin}.
 */
@Component
public class AutenticacaoAdminInterceptor implements HandlerInterceptor {

    private final SessaoAdministrador sessaoAdministrador;

    public AutenticacaoAdminInterceptor(SessaoAdministrador sessaoAdministrador) {
        this.sessaoAdministrador = sessaoAdministrador;
    }

    @Override
    public boolean preHandle(HttpServletRequest requisicao, HttpServletResponse resposta, Object handler)
            throws Exception {
        String caminho = requisicao.getRequestURI().substring(requisicao.getContextPath().length());
        if (livre(caminho)) {
            return true;
        }
        if (sessaoAdministrador.autenticado(requisicao.getSession(false))) {
            // Sem isto, o botão "voltar" do navegador mostra telas do painel
            // depois que a pessoa já saiu — inclusive em computador compartilhado.
            resposta.setHeader("Cache-Control", "no-store");
            return true;
        }
        if (caminho.startsWith("/api/")) {
            resposta.sendError(HttpStatus.UNAUTHORIZED.value());
        } else {
            resposta.sendRedirect(requisicao.getContextPath() + "/admin");
        }
        return false;
    }

    private boolean livre(String caminho) {
        return caminho.equals("/")
                || caminho.equals("/inscricao")
                || caminho.startsWith("/inscricao/")
                || caminho.equals("/admin")
                || caminho.startsWith("/admin/")
                || caminho.equals("/error")
                || caminho.equals("/acessibilidade")
                || caminho.startsWith("/acessibilidade/");
    }
}
