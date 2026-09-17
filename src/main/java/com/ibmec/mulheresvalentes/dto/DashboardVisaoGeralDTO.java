package com.ibmec.mulheresvalentes.dto;

public record DashboardVisaoGeralDTO(
        long participantesAtivos,
        long atividadesAtivas,
        long encontrosNoMes,
        long registrosPresencaNoMes) {
}
