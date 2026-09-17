package com.ibmec.mulheresvalentes.dto;

import java.math.BigDecimal;

public record DashboardFinanceiroDTO(
        BigDecimal saldoAtual,
        BigDecimal entradasNoMes,
        BigDecimal saidasNoMes,
        long movimentacoesNoMes) {
}
