package com.ibmec.mulheresvalentes.security;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * O console do H2 é um servlet próprio: ele não passa pelo Spring MVC e, por
 * isso, o {@link AutenticacaoAdminInterceptor} não o alcança. Sem este filtro,
 * qualquer pessoa abriria {@code /h2-console} e teria acesso total ao banco.
 *
 * <p>Registrado só para o caminho do console em
 * {@link com.ibmec.mulheresvalentes.config.WebConfig} — como {@code @Component}
 * ele valeria para o site inteiro e fecharia a página pública.</p>
 */
public class ConsoleH2Filter implements Filter {

    private final SessaoAdministrador sessaoAdministrador;

    public ConsoleH2Filter(SessaoAdministrador sessaoAdministrador) {
        this.sessaoAdministrador = sessaoAdministrador;
    }

    @Override
    public void doFilter(ServletRequest requisicao, ServletResponse resposta, FilterChain cadeia)
            throws IOException, ServletException {
        HttpServletRequest http = (HttpServletRequest) requisicao;
        if (!sessaoAdministrador.autenticado(http.getSession(false))) {
            ((HttpServletResponse) resposta).sendRedirect(http.getContextPath() + "/admin");
            return;
        }
        cadeia.doFilter(requisicao, resposta);
    }
}
