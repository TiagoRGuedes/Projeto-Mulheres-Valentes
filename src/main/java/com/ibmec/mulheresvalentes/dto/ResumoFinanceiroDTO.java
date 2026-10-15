package com.ibmec.mulheresvalentes.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ResumoFinanceiroDTO(
        LocalDate dataInicial,
        LocalDate dataFinal,
        BigDecimal totalEntradas,
        BigDecimal totalSaidas,
        BigDecimal saldoPeriodo,
        BigDecimal saldoAtual,
        long quantidadeMovimentacoes,
        long quantidadeDoacoesMaterial) {
}
