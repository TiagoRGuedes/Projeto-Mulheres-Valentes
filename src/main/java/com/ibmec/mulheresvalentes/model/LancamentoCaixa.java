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
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "lancamentos_caixa",
        indexes = {
            @Index(name = "idx_lancamento_caixa_data", columnList = "data"),
            @Index(name = "idx_lancamento_caixa_tipo", columnList = "tipo"),
            @Index(name = "idx_lancamento_caixa_natureza", columnList = "natureza"),
            @Index(name = "idx_lancamento_caixa_categoria", columnList = "categoria_id")
        })
public class LancamentoCaixa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Informe a data do lançamento.")
    @Column(nullable = false)
    private LocalDate data;

    @NotNull(message = "Informe se o lançamento é uma entrada ou saída.")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private TipoLancamento tipo;

    @NotNull(message = "Informe se o lançamento tem valor em dinheiro ou é uma doação de material.")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private NaturezaLancamento natureza = NaturezaLancamento.MONETARIA;

    @NotNull(message = "Escolha uma categoria.")
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "categoria_id", nullable = false)
    private CategoriaFinanceira categoria;

    @NotBlank(message = "Informe uma descrição.")
    @Size(max = 160, message = "A descrição deve ter no máximo 160 caracteres.")
    @Column(nullable = false, length = 160)
    private String descricao;

    @DecimalMin(value = "0.00", inclusive = false, message = "Informe um valor maior que zero.")
    @Digits(integer = 8, fraction = 2, message = "Informe um valor com até 8 dígitos e 2 casas decimais.")
    @Column(precision = 10, scale = 2)
    private BigDecimal valor;

    @Size(max = 200, message = "O comprovante deve ter no máximo 200 caracteres.")
    @Column(length = 200)
    private String comprovante;

    @Size(max = 500, message = "A observação deve ter no máximo 500 caracteres.")
    @Column(length = 500)
    private String observacoes;

    @Column(nullable = false, updatable = false)
    private LocalDateTime registradoEm;

    @Column(nullable = false)
    private LocalDateTime atualizadoEm;

    @Column(nullable = false, columnDefinition = "boolean default false")
    private boolean cancelado = false;

    private LocalDateTime canceladoEm;

    protected LancamentoCaixa() {
    }

    public LancamentoCaixa(
            LocalDate data,
            TipoLancamento tipo,
            CategoriaFinanceira categoria,
            String descricao,
            BigDecimal valor,
            String observacoes) {
        this(data, tipo, NaturezaLancamento.MONETARIA, categoria, descricao, valor, null, observacoes);
    }

    public LancamentoCaixa(
            LocalDate data,
            TipoLancamento tipo,
            NaturezaLancamento natureza,
            CategoriaFinanceira categoria,
            String descricao,
            BigDecimal valor,
            String comprovante,
            String observacoes) {
        this.data = data;
        this.tipo = tipo;
        this.natureza = natureza;
        this.categoria = categoria;
        this.descricao = normalizarTexto(descricao);
        this.valor = valor;
        this.comprovante = normalizarTextoOpcional(comprovante);
        this.observacoes = normalizarTextoOpcional(observacoes);
    }

    @PrePersist
    void antesDeInserir() {
        LocalDateTime agora = LocalDateTime.now();
        this.registradoEm = agora;
        this.atualizadoEm = agora;
    }

    @PreUpdate
    void antesDeAtualizar() {
        this.atualizadoEm = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public LocalDate getData() {
        return data;
    }

    public void setData(LocalDate data) {
        this.data = data;
    }

    public TipoLancamento getTipo() {
        return tipo;
    }

    public void setTipo(TipoLancamento tipo) {
        this.tipo = tipo;
    }

    public NaturezaLancamento getNatureza() {
        return natureza;
    }

    public void setNatureza(NaturezaLancamento natureza) {
        this.natureza = natureza;
    }

    public boolean isMonetario() {
        return natureza != null && natureza.isMonetaria();
    }

    public CategoriaFinanceira getCategoria() {
        return categoria;
    }

    public void setCategoria(CategoriaFinanceira categoria) {
        this.categoria = categoria;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = normalizarTexto(descricao);
    }

    public BigDecimal getValor() {
        return valor;
    }

    public void setValor(BigDecimal valor) {
        this.valor = valor;
    }

    public String getComprovante() {
        return comprovante;
    }

    public void setComprovante(String comprovante) {
        this.comprovante = normalizarTextoOpcional(comprovante);
    }

    public String getObservacoes() {
        return observacoes;
    }

    public void setObservacoes(String observacoes) {
        this.observacoes = normalizarTextoOpcional(observacoes);
    }

    public LocalDateTime getRegistradoEm() {
        return registradoEm;
    }

    public LocalDateTime getAtualizadoEm() {
        return atualizadoEm;
    }

    public boolean isCancelado() {
        return cancelado;
    }

    public LocalDateTime getCanceladoEm() {
        return canceladoEm;
    }

    public void cancelar() {
        this.cancelado = true;
        this.canceladoEm = LocalDateTime.now();
    }

    private static String normalizarTexto(String texto) {
        return texto == null ? null : texto.trim().replaceAll("\\s+", " ");
    }

    private static String normalizarTextoOpcional(String texto) {
        String normalizado = normalizarTexto(texto);
        return normalizado == null || normalizado.isBlank() ? null : normalizado;
    }
}
