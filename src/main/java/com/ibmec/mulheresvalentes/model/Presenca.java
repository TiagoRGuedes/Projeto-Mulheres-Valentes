package com.ibmec.mulheresvalentes.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "presencas",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_presenca_participante_encontro",
                columnNames = {"participante_id", "encontro_id"}
        ),
        indexes = @Index(name = "idx_presenca_data", columnList = "data_presenca")
)
public class Presenca {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Escolha o participante.")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "participante_id", nullable = false)
    private Participante participante;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "encontro_id")
    private Encontro encontro;

    @NotNull(message = "Informe a data da presença.")
    @Column(name = "data_presenca", nullable = false)
    private LocalDate data;

    @NotNull(message = "Informe a situação da presença.")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusPresenca status;

    @Size(max = 1000, message = "As observações devem ter no máximo 1000 caracteres.")
    @Column(length = 1000)
    private String observacoes;

    @Column(name = "registrado_em", nullable = false)
    private LocalDateTime registradoEm;

    protected Presenca() {
    }

    public Presenca(
            Participante participante,
            LocalDate data,
            StatusPresenca status,
            String observacoes
    ) {
        this.participante = participante;
        this.data = data;
        this.status = status;
        this.observacoes = observacoes;
    }

    public Presenca(
            Participante participante,
            Encontro encontro,
            StatusPresenca status,
            String observacoes
    ) {
        this.participante = participante;
        this.encontro = encontro;
        this.data = encontro == null ? null : encontro.getData();
        this.status = status;
        this.observacoes = observacoes;
    }

    @PrePersist
    void aoRegistrar() {
        registradoEm = LocalDateTime.now();
    }

    public void corrigir(StatusPresenca status, String observacoes) {
        this.status = status;
        this.observacoes = observacoes;
    }

    public Long getId() {
        return id;
    }

    public Participante getParticipante() {
        return participante;
    }

    public void setParticipante(Participante participante) {
        this.participante = participante;
    }

    public Encontro getEncontro() {
        return encontro;
    }

    public void setEncontro(Encontro encontro) {
        this.encontro = encontro;
        if (encontro != null) {
            this.data = encontro.getData();
        }
    }

    public LocalDate getData() {
        return data;
    }

    public void setData(LocalDate data) {
        this.data = data;
    }

    public boolean isVinculadaAEncontro() {
        return encontro != null;
    }

    public String getContexto() {
        return encontro == null ? "Presença geral" : encontro.getAtividade().getNome();
    }

    public StatusPresenca getStatus() {
        return status;
    }

    public void setStatus(StatusPresenca status) {
        this.status = status;
    }

    public String getObservacoes() {
        return observacoes;
    }

    public void setObservacoes(String observacoes) {
        this.observacoes = observacoes;
    }

    public LocalDateTime getRegistradoEm() {
        return registradoEm;
    }
}
