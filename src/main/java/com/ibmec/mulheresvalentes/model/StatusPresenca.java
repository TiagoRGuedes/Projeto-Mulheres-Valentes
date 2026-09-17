package com.ibmec.mulheresvalentes.model;

public enum StatusPresenca {
    PRESENTE("Presente"),
    AUSENTE("Ausente"),
    JUSTIFICADA("Ausência justificada");

    private final String descricao;

    StatusPresenca(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
