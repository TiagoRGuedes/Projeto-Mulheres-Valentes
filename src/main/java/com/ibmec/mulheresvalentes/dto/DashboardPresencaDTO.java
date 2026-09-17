package com.ibmec.mulheresvalentes.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record DashboardPresencaDTO(
        long presentesNoMes,
        long ausentesNoMes,
        long justificadasNoMes,
        BigDecimal frequenciaMedia,
        Long ultimoEncontroId,
        String ultimaAtividade,
        LocalDate dataUltimoEncontro,
        long presentesUltimoEncontro,
        long registrosUltimoEncontro,
        BigDecimal percentualUltimoEncontro) {
}
