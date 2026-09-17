package com.ibmec.mulheresvalentes.dto;

import java.math.BigDecimal;

public record ResumoFrequenciaDTO(
        long participantesEncontrados,
        long presencas,
        long ausencias,
        long justificadas,
        BigDecimal frequenciaMedia) {
}
