package com.ibmec.mulheresvalentes.dto;

import java.util.List;

public record ExtratoCaixaDTO(
        ResumoFinanceiroDTO resumo,
        List<LancamentoCaixaResponse> lancamentos) {
}
