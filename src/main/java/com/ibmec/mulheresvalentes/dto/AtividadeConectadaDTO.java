package com.ibmec.mulheresvalentes.dto;

import com.ibmec.mulheresvalentes.model.Encontro;

import java.util.List;

public record AtividadeConectadaDTO(
        long totalEncontros,
        ResumoPresencasDto resumoPresencas,
        List<Encontro> encontrosRecentes
) {
}
