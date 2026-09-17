package com.ibmec.mulheresvalentes.dto;

import com.ibmec.mulheresvalentes.model.Encontro;

import java.util.List;

/** Visão consolidada de uma atividade e dos registros que dependem dela. */
public record AtividadeConectadaDTO(
        long totalEncontros,
        ResumoPresencasDto resumoPresencas,
        List<Encontro> encontrosRecentes
) {
}
