package com.ibmec.mulheresvalentes.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

/** Acesso simples à pessoa logada na requisição atual. */
public final class UsuarioLogado {

    private UsuarioLogado() {
    }

    public static Optional<UsuarioAutenticado> atual() {
        Authentication autenticacao = SecurityContextHolder.getContext().getAuthentication();
        if (autenticacao != null
                && autenticacao.isAuthenticated()
                && autenticacao.getPrincipal() instanceof UsuarioAutenticado usuario) {
            return Optional.of(usuario);
        }
        return Optional.empty();
    }
}
