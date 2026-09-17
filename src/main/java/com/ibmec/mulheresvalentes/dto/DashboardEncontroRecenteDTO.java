package com.ibmec.mulheresvalentes.dto;

import java.time.LocalDate;

public record DashboardEncontroRecenteDTO(
        Long id,
        Long atividadeId,
        String atividade,
        LocalDate data,
        long registrosPresenca,
        String situacao) {
}
