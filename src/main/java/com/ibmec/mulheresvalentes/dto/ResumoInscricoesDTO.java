package com.ibmec.mulheresvalentes.dto;

public record ResumoInscricoesDTO(long novas, long emContato, long aceitas, long arquivadas) {

    public long abertas() {
        return novas + emContato;
    }

    public long total() {
        return novas + emContato + aceitas + arquivadas;
    }
}
