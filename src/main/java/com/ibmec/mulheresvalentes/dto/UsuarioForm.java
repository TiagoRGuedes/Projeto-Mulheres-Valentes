package com.ibmec.mulheresvalentes.dto;

import com.ibmec.mulheresvalentes.model.PerfilUsuario;
import com.ibmec.mulheresvalentes.model.Usuario;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Formulário de cadastro e edição de usuário. A senha só é usada no cadastro. */
public class UsuarioForm {

    @NotBlank(message = "Informe o nome da pessoa.")
    @Size(max = 150, message = "O nome deve ter no máximo 150 caracteres.")
    private String nome;

    @Size(max = 40, message = "O login deve ter no máximo 40 caracteres.")
    private String login;

    @NotNull(message = "Escolha o perfil de acesso.")
    private PerfilUsuario perfil = PerfilUsuario.OPERADOR;

    private String senha;

    private String confirmacaoSenha;

    public static UsuarioForm de(Usuario usuario) {
        UsuarioForm form = new UsuarioForm();
        form.setNome(usuario.getNome());
        form.setLogin(usuario.getLogin());
        form.setPerfil(usuario.getPerfil());
        return form;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public PerfilUsuario getPerfil() {
        return perfil;
    }

    public void setPerfil(PerfilUsuario perfil) {
        this.perfil = perfil;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public String getConfirmacaoSenha() {
        return confirmacaoSenha;
    }

    public void setConfirmacaoSenha(String confirmacaoSenha) {
        this.confirmacaoSenha = confirmacaoSenha;
    }
}
