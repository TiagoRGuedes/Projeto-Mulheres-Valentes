package com.ibmec.mulheresvalentes.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * Acesso da equipe ao painel (RNF04), em versão simples: uma única conta
 * administrativa guardada em configuração e marcada na sessão do navegador.
 */
@Component
public class SessaoAdministrador {

    public static final String CHAVE_SESSAO = "administradorAutenticado";

    private final String usuarioConfigurado;
    private final String senhaConfigurada;

    public SessaoAdministrador(
            @Value("${app.admin.usuario}") String usuarioConfigurado,
            @Value("${app.admin.senha}") String senhaConfigurada) {
        this.usuarioConfigurado = usuarioConfigurado;
        this.senhaConfigurada = senhaConfigurada;
    }

    public boolean autenticado(HttpSession sessao) {
        return sessao != null && Boolean.TRUE.equals(sessao.getAttribute(CHAVE_SESSAO));
    }

    public boolean entrar(HttpServletRequest requisicao, String usuario, String senha) {
        if (!credenciaisConferem(usuario, senha)) {
            return false;
        }
        requisicao.getSession(true);
        requisicao.changeSessionId();
        requisicao.getSession().setAttribute(CHAVE_SESSAO, Boolean.TRUE);
        return true;
    }

    public void sair(HttpSession sessao) {
        if (sessao != null) {
            sessao.invalidate();
        }
    }

    private boolean credenciaisConferem(String usuario, String senha) {
        boolean usuarioConfere = igual(usuarioConfigurado, usuario);
        boolean senhaConfere = igual(senhaConfigurada, senha);
        return usuarioConfere && senhaConfere;
    }

    private static boolean igual(String esperado, String informado) {
        byte[] bytesInformado = informado == null
                ? new byte[0]
                : informado.getBytes(StandardCharsets.UTF_8);
        return MessageDigest.isEqual(esperado.getBytes(StandardCharsets.UTF_8), bytesInformado);
    }
}
