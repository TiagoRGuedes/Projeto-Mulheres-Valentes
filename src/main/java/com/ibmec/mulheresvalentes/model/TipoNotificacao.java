package com.ibmec.mulheresvalentes.model;

public enum TipoNotificacao {
    NOVA_INSCRICAO("Nova inscrição", "✉"),
    INSCRICAO_SEM_CONTATO("Esperando contato", "!");

    private final String descricao;
    private final String simbolo;

    TipoNotificacao(String descricao, String simbolo) {
        this.descricao = descricao;
        this.simbolo = simbolo;
    }

    public String getDescricao() {
        return descricao;
    }

    public String getSimbolo() {
        return simbolo;
    }
}
