package com.ibmec.mulheresvalentes.dto;

public record ResumoRegistroPresencaDto(
        long total,
        long presentes,
        long ausentes,
        long justificadas
) {
}
