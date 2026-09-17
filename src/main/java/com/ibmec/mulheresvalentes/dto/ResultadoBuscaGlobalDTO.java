package com.ibmec.mulheresvalentes.dto;

import java.util.List;

public record ResultadoBuscaGlobalDTO(
        String termo,
        List<GrupoResultadoBuscaDTO> grupos,
        int limitePorGrupo) {

    public ResultadoBuscaGlobalDTO {
        termo = termo == null ? "" : termo;
        grupos = grupos == null ? List.of() : List.copyOf(grupos);
    }

    public boolean pesquisaRealizada() {
        return !termo.isBlank();
    }

    public boolean temResultados() {
        return totalResultados() > 0;
    }

    public int totalResultados() {
        return grupos.stream().mapToInt(GrupoResultadoBuscaDTO::quantidade).sum();
    }
}
