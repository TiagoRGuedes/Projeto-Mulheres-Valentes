package com.ibmec.mulheresvalentes.model;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
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
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(
        name = "inscricoes",
        indexes = {
            @Index(name = "idx_inscricao_status", columnList = "status"),
            @Index(name = "idx_inscricao_recebida_em", columnList = "recebida_em")
        })
public class Inscricao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String nome;

    @Column(nullable = false, length = 120)
    private String contato;

    @Enumerated(EnumType.STRING)
    @Column(name = "preferencia_contato", nullable = false, length = 20)
    private PreferenciaContato preferenciaContato;

    @Column(name = "contato_seguro", length = 200)
    private String contatoSeguro;

    @Column(length = 100)
    private String bairro;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "inscricao_areas", joinColumns = @JoinColumn(name = "inscricao_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "area", length = 30)
    private Set<AreaApoio> areas = new LinkedHashSet<>();

    @Column(length = 1000)
    private String mensagem;

    @Column(nullable = false)
    private boolean consentimento;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusInscricao status = StatusInscricao.NOVA;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "participante_id")
    private Participante participante;

    @Column(name = "recebida_em", nullable = false, updatable = false)
    private LocalDateTime recebidaEm;

    @Column(name = "atualizada_em", nullable = false)
    private LocalDateTime atualizadaEm;

    protected Inscricao() {
    }

    public Inscricao(
            String nome,
            String contato,
            PreferenciaContato preferenciaContato,
            String contatoSeguro,
            String bairro,
            Set<AreaApoio> areas,
            String mensagem,
            boolean consentimento) {
        this.nome = nome;
        this.contato = contato;
        this.preferenciaContato = preferenciaContato;
        this.contatoSeguro = contatoSeguro;
        this.bairro = bairro;
        this.areas = areas == null || areas.isEmpty()
                ? new LinkedHashSet<>()
                : new LinkedHashSet<>(EnumSet.copyOf(areas));
        this.mensagem = mensagem;
        this.consentimento = consentimento;
    }

    @PrePersist
    void aoReceber() {
        LocalDateTime agora = LocalDateTime.now();
        recebidaEm = agora;
        atualizadaEm = agora;
    }

    @PreUpdate
    void aoAtualizar() {
        atualizadaEm = LocalDateTime.now();
    }

    public void marcarEmContato() {
        this.status = StatusInscricao.EM_CONTATO;
    }

    public void aceitar(Participante participante) {
        this.participante = participante;
        this.status = StatusInscricao.ACEITA;
    }

    public void arquivar() {
        this.status = StatusInscricao.ARQUIVADA;
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getContato() {
        return contato;
    }

    public PreferenciaContato getPreferenciaContato() {
        return preferenciaContato;
    }

    public String getContatoSeguro() {
        return contatoSeguro;
    }

    public String getBairro() {
        return bairro;
    }

    public Set<AreaApoio> getAreas() {
        return areas;
    }

    public String getMensagem() {
        return mensagem;
    }

    public boolean isConsentimento() {
        return consentimento;
    }

    public StatusInscricao getStatus() {
        return status;
    }

    public Participante getParticipante() {
        return participante;
    }

    public LocalDateTime getRecebidaEm() {
        return recebidaEm;
    }

    public LocalDateTime getAtualizadaEm() {
        return atualizadaEm;
    }
}
