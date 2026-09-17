package com.ibmec.mulheresvalentes.dto;

import com.ibmec.mulheresvalentes.model.TipoLancamento;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.Locale;

public class LancamentoCaixaFiltro {

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dataInicial;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dataFinal;

    private TipoLancamento tipo;
    private Long categoriaId;
    private String descricao;

    public LocalDate getDataInicial() {
        return dataInicial;
    }

    public void setDataInicial(LocalDate dataInicial) {
        this.dataInicial = dataInicial;
    }

    public LocalDate getDataFinal() {
        return dataFinal;
    }

    public void setDataFinal(LocalDate dataFinal) {
        this.dataFinal = dataFinal;
    }

    public TipoLancamento getTipo() {
        return tipo;
    }

    public void setTipo(TipoLancamento tipo) {
        this.tipo = tipo;
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

    public String getDescricaoNormalizada() {
        if (descricao == null || descricao.isBlank()) {
            return null;
        }
        return descricao.trim().toLowerCase(Locale.ROOT);
    }
}
