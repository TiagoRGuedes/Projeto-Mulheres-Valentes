package com.ibmec.mulheresvalentes.dto;

import com.ibmec.mulheresvalentes.model.LancamentoCaixa;
import com.ibmec.mulheresvalentes.model.NaturezaLancamento;
import com.ibmec.mulheresvalentes.model.TipoLancamento;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;

public class LancamentoCaixaForm {

    @NotNull(message = "Informe a data do lançamento.")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate data;

    @NotNull(message = "Informe se o lançamento tem valor em dinheiro ou é uma doação de material.")
    private NaturezaLancamento natureza = NaturezaLancamento.MONETARIA;

    @NotNull(message = "Escolha uma categoria.")
    private Long categoriaId;

    @NotBlank(message = "Informe uma descrição.")
    @Size(max = 160, message = "A descrição deve ter no máximo 160 caracteres.")
    private String descricao;

    @DecimalMin(value = "0.00", inclusive = false, message = "Informe um valor maior que zero.")
    @Digits(integer = 8, fraction = 2, message = "Use no máximo 8 dígitos e 2 casas decimais.")
    private BigDecimal valor;

    @Size(max = 200, message = "O comprovante deve ter no máximo 200 caracteres.")
    private String comprovante;

    @Size(max = 500, message = "A observação deve ter no máximo 500 caracteres.")
    private String observacoes;

    private TipoLancamento tipo;

    public LancamentoCaixaForm() {
        this.data = LocalDate.now();
    }

    public static LancamentoCaixaForm from(LancamentoCaixa lancamento) {
        LancamentoCaixaForm form = new LancamentoCaixaForm();
        form.setData(lancamento.getData());
        form.setNatureza(lancamento.getNatureza());
        form.setCategoriaId(lancamento.getCategoria().getId());
        form.setDescricao(lancamento.getDescricao());
        form.setValor(lancamento.getValor());
        form.setComprovante(lancamento.getComprovante());
        form.setObservacoes(lancamento.getObservacoes());
        form.setTipo(lancamento.getTipo());
        return form;
    }

    public boolean isMonetario() {
        return natureza == null || natureza.isMonetaria();
    }

    public LocalDate getData() {
        return data;
    }

    public void setData(LocalDate data) {
        this.data = data;
    }

    public NaturezaLancamento getNatureza() {
        return natureza;
    }

    public void setNatureza(NaturezaLancamento natureza) {
        this.natureza = natureza;
    }

    public Long getCategoriaId() {
        return categoriaId;
    }

    public void setCategoriaId(Long categoriaId) {
        this.categoriaId = categoriaId;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
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
        this.comprovante = comprovante;
    }

    public String getObservacoes() {
        return observacoes;
    }

    public void setObservacoes(String observacoes) {
        this.observacoes = observacoes;
    }

    public TipoLancamento getTipo() {
        return tipo;
    }

    public void setTipo(TipoLancamento tipo) {
        this.tipo = tipo;
    }
}
