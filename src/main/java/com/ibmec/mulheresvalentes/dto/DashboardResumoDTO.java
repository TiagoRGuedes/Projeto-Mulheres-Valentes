package com.ibmec.mulheresvalentes.dto;

import com.ibmec.mulheresvalentes.model.HistoricoOperacao;
import com.ibmec.mulheresvalentes.model.LancamentoCaixa;

import java.util.List;

public record DashboardResumoDTO(
        DashboardVisaoGeralDTO visaoGeral,
        DashboardPresencaDTO presenca,
        DashboardFinanceiroDTO financeiro,
        List<LancamentoCaixa> movimentacoesRecentes,
        List<DashboardEncontroRecenteDTO> encontrosRecentes,
        List<HistoricoOperacao> atividadeRecente) {
}
