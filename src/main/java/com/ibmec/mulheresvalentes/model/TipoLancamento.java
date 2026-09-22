package com.ibmec.mulheresvalentes.model;

public enum TipoLancamento {
    ENTRADA("Entrada", "↑"),
    SAIDA("Saída", "↓");

    private final String descricao;
    private final String simbolo;

    TipoLancamento(String descricao, String simbolo) {
        this.descricao = descricao;
        this.simbolo = simbolo;
    }

    public String getDescricao() {
        return descricao;
    }

    public String getSimbolo() {
        return simbolo;
    }

    public String getRotuloAcessivel() {
        return simbolo + " " + descricao;
    }
}
