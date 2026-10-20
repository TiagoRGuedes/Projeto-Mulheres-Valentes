package com.ibmec.mulheresvalentes.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

/** Aviso para a equipe, mostrado no sino do topo do painel. */
@Entity
@Table(name = "notificacoes", indexes = {
    @Index(name = "idx_notificacao_criada_em", columnList = "criada_em"),
    @Index(name = "idx_notificacao_referencia", columnList = "tipo, referencia_id")
})
public class Notificacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 30)
    private TipoNotificacao tipo;

    @Column(nullable = false, length = 150)
    private String titulo;

    @Column(nullable = false, length = 300)
    private String mensagem;

    @Column(length = 200)
    private String link;

    @Column(name = "referencia_id")
    private Long referenciaId;

    @Column(name = "criada_em", nullable = false, updatable = false)
    private LocalDateTime criadaEm;

    protected Notificacao() {
    }

    public Notificacao(TipoNotificacao tipo, String titulo, String mensagem, String link, Long referenciaId) {
        this.tipo = tipo;
        this.titulo = titulo;
        this.mensagem = mensagem;
        this.link = link;
        this.referenciaId = referenciaId;
    }

    @PrePersist
    void aoCriar() {
        if (criadaEm == null) {
            criadaEm = LocalDateTime.now();
        }
    }

    public Long getId() {
        return id;
    }

    public TipoNotificacao getTipo() {
        return tipo;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getMensagem() {
        return mensagem;
    }

    public String getLink() {
        return link;
    }

    public Long getReferenciaId() {
        return referenciaId;
    }

    public LocalDateTime getCriadaEm() {
        return criadaEm;
    }
}
