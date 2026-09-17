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
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

/**
 * Registro imutável e somente leitura das alterações administrativas.
 * Não guarda observações privadas nem dados pessoais além do nome necessário
 * para que a ação possa ser compreendida.
 */
@Entity
@Table(
        name = "historico_operacoes",
        indexes = {
            @Index(name = "idx_historico_data_hora", columnList = "data_hora"),
            @Index(name = "idx_historico_modulo", columnList = "modulo"),
            @Index(name = "idx_historico_entidade", columnList = "tipo_entidade, entidade_id")
        })
public class HistoricoOperacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ModuloHistorico modulo;

    @NotBlank
    @Size(max = 80)
    @Column(name = "tipo_entidade", nullable = false, length = 80)
    private String tipoEntidade;

    @Column(name = "entidade_id")
    private Long entidadeId;

    @NotBlank
    @Size(max = 180)
    @Column(name = "nome_entidade", nullable = false, length = 180)
    private String nomeEntidade;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private AcaoHistorico acao;

    @NotBlank
    @Size(max = 1000)
    @Column(nullable = false, length = 1000)
    private String descricao;

    @Size(max = 1000)
    @Column(name = "valor_anterior_resumo", length = 1000)
    private String valorAnteriorResumo;

    @Size(max = 1000)
    @Column(name = "valor_novo_resumo", length = 1000)
    private String valorNovoResumo;

    @Column(name = "data_hora", nullable = false, updatable = false)
    private LocalDateTime dataHora;

    @Column(name = "usuario_id")
    private Long usuarioId;

    @Size(max = 150)
    @Column(name = "usuario_nome", length = 150)
    private String usuarioNome;

    protected HistoricoOperacao() {
    }

    public HistoricoOperacao(
            ModuloHistorico modulo,
            String tipoEntidade,
            Long entidadeId,
            String nomeEntidade,
            AcaoHistorico acao,
            String descricao,
            String valorAnteriorResumo,
            String valorNovoResumo) {
        this.modulo = modulo;
        this.tipoEntidade = normalizar(tipoEntidade);
        this.entidadeId = entidadeId;
        this.nomeEntidade = normalizar(nomeEntidade);
        this.acao = acao;
        this.descricao = normalizar(descricao);
        this.valorAnteriorResumo = normalizarOpcional(valorAnteriorResumo);
        this.valorNovoResumo = normalizarOpcional(valorNovoResumo);
    }

    @PrePersist
    void antesDeInserir() {
        this.dataHora = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public ModuloHistorico getModulo() {
        return modulo;
    }

    public String getTipoEntidade() {
        return tipoEntidade;
    }

    public Long getEntidadeId() {
        return entidadeId;
    }

    public String getNomeEntidade() {
        return nomeEntidade;
    }

    public AcaoHistorico getAcao() {
        return acao;
    }

    public String getDescricao() {
        return descricao;
    }

    public String getValorAnteriorResumo() {
        return valorAnteriorResumo;
    }

    public String getValorNovoResumo() {
        return valorNovoResumo;
    }

    public LocalDateTime getDataHora() {
        return dataHora;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public String getUsuarioNome() {
        return usuarioNome;
    }

    private static String normalizar(String valor) {
        return valor == null ? null : valor.trim().replaceAll("\\s+", " ");
    }

    private static String normalizarOpcional(String valor) {
        String normalizado = normalizar(valor);
        return normalizado == null || normalizado.isBlank() ? null : normalizado;
    }
}
