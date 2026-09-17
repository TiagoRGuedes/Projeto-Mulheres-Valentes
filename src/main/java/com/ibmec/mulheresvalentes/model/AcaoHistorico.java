package com.ibmec.mulheresvalentes.model;

/**
 * Ações administrativas que podem aparecer no Histórico do Sistema.
 */
public enum AcaoHistorico {
    CRIACAO("Criação", "+"),
    EDICAO("Edição", "✎"),
    ATIVACAO("Ativação", "✓"),
    DESATIVACAO("Desativação", "−"),
    EXCLUSAO("Exclusão", "×"),
    REGISTRO("Registro", "✓"),
    CORRECAO("Correção", "✎");

    private final String descricao;
    private final String simbolo;

    AcaoHistorico(String descricao, String simbolo) {
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
