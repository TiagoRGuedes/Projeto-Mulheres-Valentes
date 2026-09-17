package com.ibmec.mulheresvalentes.dto;

import com.ibmec.mulheresvalentes.model.Presenca;

import java.util.List;

/** Resumo da chamada ligada a um encontro específico. */
public record EncontroConectadoDTO(
        ResumoPresencasDto resumo,
        List<Presenca> registros
) {
    public boolean possuiChamada() {
        return resumo != null && resumo.total() > 0;
    }
}
