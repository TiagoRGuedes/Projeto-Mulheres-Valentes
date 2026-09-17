package com.ibmec.mulheresvalentes.dto;

import java.util.Map;

public record ResultadoOperacaoDTO(
        String destino,
        String mensagem,
        Map<String, Object> atributosFlash
) {
    public ResultadoOperacaoDTO(String destino, String mensagem) {
        this(destino, mensagem, Map.of());
    }
}
