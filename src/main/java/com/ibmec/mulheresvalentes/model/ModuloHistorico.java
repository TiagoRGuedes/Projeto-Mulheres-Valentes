package com.ibmec.mulheresvalentes.model;

public enum ModuloHistorico {
    PARTICIPANTES("Participantes"),
    ATIVIDADES("Atividades"),
    ENCONTROS("Encontros"),
    PRESENCAS("Presenças"),
    LIVRO_CAIXA("Livro Caixa"),
    CATEGORIAS("Categorias");

    private final String descricao;

    ModuloHistorico(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
