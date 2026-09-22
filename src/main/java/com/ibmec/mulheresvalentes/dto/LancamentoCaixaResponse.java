package com.ibmec.mulheresvalentes.dto;

import com.ibmec.mulheresvalentes.model.LancamentoCaixa;
import com.ibmec.mulheresvalentes.model.NaturezaLancamento;
import com.ibmec.mulheresvalentes.model.TipoLancamento;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record LancamentoCaixaResponse(
        Long id,
        LocalDate data,
        TipoLancamento tipo,
        String rotuloTipo,
        NaturezaLancamento natureza,
        String rotuloNatureza,
        Long categoriaId,
        String categoria,
        String descricao,
        BigDecimal valor,
        String comprovante,
        String observacoes,
        LocalDateTime registradoEm,
        LocalDateTime atualizadoEm) {

    public static LancamentoCaixaResponse from(LancamentoCaixa lancamento) {
        return new LancamentoCaixaResponse(
                lancamento.getId(),
                lancamento.getData(),
                lancamento.getTipo(),
                lancamento.getTipo().getRotuloAcessivel(),
                lancamento.getNatureza(),
                lancamento.getNatureza().getDescricao(),
                lancamento.getCategoria().getId(),
                lancamento.getCategoria().getNome(),
                lancamento.getDescricao(),
                lancamento.getValor(),
                lancamento.getComprovante(),
                lancamento.getObservacoes(),
                lancamento.getRegistradoEm(),
                lancamento.getAtualizadoEm());
    }
}
