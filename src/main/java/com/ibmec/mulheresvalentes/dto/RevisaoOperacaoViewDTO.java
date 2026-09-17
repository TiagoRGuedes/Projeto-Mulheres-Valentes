package com.ibmec.mulheresvalentes.dto;

import java.util.List;

public record RevisaoOperacaoViewDTO(
        String contexto,
        String titulo,
        String descricao,
        String simbolo,
        String tom,
        String textoConfirmar,
        List<LinhaRevisaoDTO> campos,
        List<ComparacaoRevisaoDTO> comparacoes,
        List<String> impactos
) {
}
