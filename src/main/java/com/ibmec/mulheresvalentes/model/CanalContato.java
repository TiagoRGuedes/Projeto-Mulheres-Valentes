package com.ibmec.mulheresvalentes.model;

public enum CanalContato {
    WHATSAPP("WhatsApp"),
    LIGACAO("Ligação"),
    SMS("SMS"),
    EMAIL("E-mail"),
    PRESENCIAL("Pessoalmente"),
    OUTRO("Outro");

    private final String descricao;

    CanalContato(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }

    /** Canal sugerido a partir da forma de contato que a mulher preferiu na inscrição. */
    public static CanalContato sugeridoPara(PreferenciaContato preferencia) {
        if (preferencia == null) {
            return WHATSAPP;
        }
        return switch (preferencia) {
            case WHATSAPP -> WHATSAPP;
            case LIGACAO -> LIGACAO;
            case EMAIL -> EMAIL;
        };
    }
}
