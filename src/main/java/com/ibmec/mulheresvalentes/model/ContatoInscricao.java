package com.ibmec.mulheresvalentes.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

/**
 * Uma tentativa de contato da equipe com uma mulher que se inscreveu.
 * Os registros só são acrescentados, nunca editados, para manter o acompanhamento fiel.
 */
@Entity
@Table(name = "contatos_inscricao", indexes = @Index(name = "idx_contato_inscricao", columnList = "inscricao_id"))
public class ContatoInscricao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "inscricao_id", nullable = false)
    private Inscricao inscricao;

    @Column(name = "realizado_em", nullable = false)
    private LocalDateTime realizadoEm;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 20)
    private CanalContato canal;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 30)
    private ResultadoContato resultado;

    @Column(length = 1000)
    private String observacao;

    @Column(name = "registrado_por_id")
    private Long registradoPorId;

    @Column(name = "registrado_por_nome", length = 150)
    private String registradoPorNome;

    @Column(name = "registrado_em", nullable = false, updatable = false)
    private LocalDateTime registradoEm;

    protected ContatoInscricao() {
    }

    public ContatoInscricao(
            Inscricao inscricao,
            LocalDateTime realizadoEm,
            CanalContato canal,
            ResultadoContato resultado,
            String observacao,
            Long registradoPorId,
            String registradoPorNome) {
        this.inscricao = inscricao;
        this.realizadoEm = realizadoEm;
        this.canal = canal;
        this.resultado = resultado;
        this.observacao = observacao;
        this.registradoPorId = registradoPorId;
        this.registradoPorNome = registradoPorNome;
    }

    @PrePersist
    void aoRegistrar() {
        registradoEm = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Inscricao getInscricao() {
        return inscricao;
    }

    public LocalDateTime getRealizadoEm() {
        return realizadoEm;
    }

    public CanalContato getCanal() {
        return canal;
    }

    public ResultadoContato getResultado() {
        return resultado;
    }

    public String getObservacao() {
        return observacao;
    }

    public Long getRegistradoPorId() {
        return registradoPorId;
    }

    public String getRegistradoPorNome() {
        return registradoPorNome;
    }

    public LocalDateTime getRegistradoEm() {
        return registradoEm;
    }
}
