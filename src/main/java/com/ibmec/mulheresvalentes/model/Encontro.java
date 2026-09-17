package com.ibmec.mulheresvalentes.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "encontros")
public class Encontro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Escolha a atividade.")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "atividade_id", nullable = false)
    private Atividade atividade;

    @NotNull(message = "Informe a data do encontro.")
    @Column(nullable = false)
    private LocalDate data;

    @Size(max = 1000, message = "As observações devem ter no máximo 1000 caracteres.")
    @Column(length = 1000)
    private String observacoes;

    @Column(name = "data_cadastro", nullable = false, updatable = false)
    private LocalDateTime dataCadastro;

    protected Encontro() {
    }

    public Encontro(Atividade atividade, LocalDate data, String observacoes) {
        this.atividade = atividade;
        this.data = data;
        this.observacoes = observacoes;
    }

    @PrePersist
    void aoCadastrar() {
        dataCadastro = LocalDateTime.now();
    }

    public void atualizarDados(Atividade atividade, LocalDate data, String observacoes) {
        this.atividade = atividade;
        this.data = data;
        this.observacoes = observacoes;
    }

    public Long getId() {
        return id;
    }

    public Atividade getAtividade() {
        return atividade;
    }

    public void setAtividade(Atividade atividade) {
        this.atividade = atividade;
    }

    public LocalDate getData() {
        return data;
    }

    public void setData(LocalDate data) {
        this.data = data;
    }

    public String getObservacoes() {
        return observacoes;
    }

    public void setObservacoes(String observacoes) {
        this.observacoes = observacoes;
    }

    public LocalDateTime getDataCadastro() {
        return dataCadastro;
    }
}
