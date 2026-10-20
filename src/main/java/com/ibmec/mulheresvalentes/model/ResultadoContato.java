package com.ibmec.mulheresvalentes.model;

public enum ResultadoContato {
    CONVERSOU("Conseguimos conversar"),
    NAO_ATENDEU("Não atendeu / não respondeu"),
    PEDIU_RETORNO("Pediu para falar depois"),
    NAO_ERA_SEGURO("Não era um bom momento ou não era seguro"),
    CONTATO_INVALIDO("Contato errado ou inexistente"),
    OUTRO("Outro");

    private final String descricao;

    ResultadoContato(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }

    public boolean isConseguiuFalar() {
        return this == CONVERSOU;
    }
}
