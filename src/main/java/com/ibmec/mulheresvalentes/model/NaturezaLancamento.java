package com.ibmec.mulheresvalentes.model;

public enum NaturezaLancamento {
    MONETARIA("Valor em dinheiro", "R$"),
    DOACAO_MATERIAL("Doação de material", "▣");

    private final String descricao;
    private final String simbolo;

    NaturezaLancamento(String descricao, String simbolo) {
        this.descricao = descricao;
        this.simbolo = simbolo;
    }

    public String getDescricao() {
        return descricao;
    }

    public String getSimbolo() {
        return simbolo;
    }

    public boolean isMonetaria() {
        return this == MONETARIA;
    }

    public boolean exigeComprovante() {
        return this == DOACAO_MATERIAL;
    }
}
