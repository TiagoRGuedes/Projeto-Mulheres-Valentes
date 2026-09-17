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
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "presencas",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_presenca_participante_encontro",
                columnNames = {"participante_id", "encontro_id"}
        )
)
public class Presenca {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Escolha o participante.")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "participante_id", nullable = false)
    private Participante participante;

    @NotNull(message = "Escolha o encontro.")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "encontro_id", nullable = false)
    private Encontro encontro;

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
            Encontro encontro,
            StatusPresenca status,
            String observacoes
    ) {
        this.participante = participante;
        this.encontro = encontro;
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
