package com.ibmec.mulheresvalentes.security;

import com.ibmec.mulheresvalentes.model.PerfilUsuario;
import com.ibmec.mulheresvalentes.model.Usuario;
import org.springframework.security.core.CredentialsContainer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serial;
import java.util.Collection;
import java.util.List;

/**
 * Dados da pessoa logada guardados na sessão. Não guarda a senha depois do login.
 */
public class UsuarioAutenticado implements UserDetails, CredentialsContainer {

    @Serial
    private static final long serialVersionUID = 1L;

    private final Long id;
    private final String nome;
    private final String login;
    private final PerfilUsuario perfil;
    private final boolean ativo;
    private String senhaHash;

    public UsuarioAutenticado(Usuario usuario) {
        this.id = usuario.getId();
        this.nome = usuario.getNome();
        this.login = usuario.getLogin();
        this.perfil = usuario.getPerfil();
        this.ativo = usuario.isAtivo();
        this.senhaHash = usuario.getSenhaHash();
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getLogin() {
        return login;
    }

    public PerfilUsuario getPerfil() {
        return perfil;
    }

    public boolean isAdmin() {
        return perfil == PerfilUsuario.ADMIN;
    }

    public String getPrimeiroNome() {
        if (nome == null || nome.isBlank()) {
            return login;
        }
        return nome.trim().split("\\s+")[0];
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(perfil.getAutoridade()));
    }

    @Override
    public String getPassword() {
        return senhaHash;
    }

    @Override
    public String getUsername() {
        return login;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return ativo;
    }

    @Override
    public void eraseCredentials() {
        this.senhaHash = null;
    }
}
