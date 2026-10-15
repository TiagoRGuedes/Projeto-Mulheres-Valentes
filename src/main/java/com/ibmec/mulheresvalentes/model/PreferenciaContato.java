package com.ibmec.mulheresvalentes.model;

public enum PreferenciaContato {
    WHATSAPP("Mensagem de WhatsApp"),
    LIGACAO("Ligação"),
    EMAIL("E-mail");

    private final String descricao;

    PreferenciaContato(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
