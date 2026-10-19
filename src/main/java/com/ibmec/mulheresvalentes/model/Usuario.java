package com.ibmec.mulheresvalentes.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.LocalDateTime;

/**
 * Pessoa da equipe que acessa o painel administrativo.
 * A senha nunca é guardada em texto: somente o hash gerado pelo PasswordEncoder.
 */
@Entity
@Table(name = "usuarios", uniqueConstraints = @UniqueConstraint(name = "uk_usuario_login", columnNames = "login"))
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String nome;

    @Column(nullable = false, length = 40)
    private String login;

    @Column(name = "senha_hash", nullable = false, length = 255)
    private String senhaHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PerfilUsuario perfil;

    @Column(nullable = false)
    private boolean ativo = true;

    @Column(name = "deve_trocar_senha", nullable = false)
    private boolean deveTrocarSenha = true;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    @Column(name = "atualizado_em", nullable = false)
    private LocalDateTime atualizadoEm;

    @Column(name = "ultimo_acesso_em")
    private LocalDateTime ultimoAcessoEm;

    protected Usuario() {
    }

    public Usuario(String nome, String login, String senhaHash, PerfilUsuario perfil) {
        this.nome = nome;
        this.login = login;
        this.senhaHash = senhaHash;
        this.perfil = perfil;
    }

    @PrePersist
    void aoCriar() {
        LocalDateTime agora = LocalDateTime.now();
        criadoEm = agora;
        atualizadoEm = agora;
    }

    @PreUpdate
    void aoAtualizar() {
        atualizadoEm = LocalDateTime.now();
    }

    public void atualizarDados(String nome, PerfilUsuario perfil) {
        this.nome = nome;
        this.perfil = perfil;
    }

    /** Senha definida pela administração: a pessoa precisa trocá-la no próximo acesso. */
    public void definirSenhaTemporaria(String senhaHash) {
        this.senhaHash = senhaHash;
        this.deveTrocarSenha = true;
    }

    /** Senha escolhida pela própria pessoa. */
    public void definirSenhaPropria(String senhaHash) {
        this.senhaHash = senhaHash;
        this.deveTrocarSenha = false;
    }

    public void ativar() {
        this.ativo = true;
    }

    public void desativar() {
        this.ativo = false;
    }

    public void registrarAcesso() {
        this.ultimoAcessoEm = LocalDateTime.now();
    }

    public boolean isAdmin() {
        return perfil == PerfilUsuario.ADMIN;
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

    public String getSenhaHash() {
        return senhaHash;
    }

    public PerfilUsuario getPerfil() {
        return perfil;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public boolean isDeveTrocarSenha() {
        return deveTrocarSenha;
    }

    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }

    public LocalDateTime getAtualizadoEm() {
        return atualizadoEm;
    }

    public LocalDateTime getUltimoAcessoEm() {
        return ultimoAcessoEm;
    }
}
