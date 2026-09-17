package com.ibmec.mulheresvalentes.dto;

import java.util.List;

public record GrupoResultadoBuscaDTO(
        String chave,
        String titulo,
        List<ItemResultadoBuscaDTO> itens) {

    public GrupoResultadoBuscaDTO {
        itens = itens == null ? List.of() : List.copyOf(itens);
    }

    public int quantidade() {
        return itens.size();
    }
}
