package com.ibmec.mulheresvalentes.dto;

public record ResumoPresencasDto(
        long total,
        long presentes,
        long ausentes,
        long justificadas
) {
}
