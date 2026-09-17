package com.ibmec.mulheresvalentes.dto;

public record LinhaRevisaoDTO(String rotulo, String valor, boolean destaque) {
    public LinhaRevisaoDTO(String rotulo, String valor) {
        this(rotulo, valor, false);
    }
}
