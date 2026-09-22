package com.ibmec.mulheresvalentes.dto;

import com.ibmec.mulheresvalentes.model.TipoOperacaoRevisao;

public record OperacaoRevisaoDTO(
        TipoOperacaoRevisao tipo,
        Long entidadeId,
        Object dados
) {
    public OperacaoRevisaoDTO(TipoOperacaoRevisao tipo, Object dados) {
        this(tipo, null, dados);
    }

    public OperacaoRevisaoDTO(TipoOperacaoRevisao tipo, Long entidadeId) {
        this(tipo, entidadeId, null);
    }
}
