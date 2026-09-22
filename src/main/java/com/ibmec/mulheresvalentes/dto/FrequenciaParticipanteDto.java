package com.ibmec.mulheresvalentes.dto;

import java.math.BigDecimal;

public record FrequenciaParticipanteDto(
        Long participanteId,
        String participanteNome,
        long registros,
        long presencas,
        long ausencias,
        long justificadas,
        BigDecimal percentual
) {
}
