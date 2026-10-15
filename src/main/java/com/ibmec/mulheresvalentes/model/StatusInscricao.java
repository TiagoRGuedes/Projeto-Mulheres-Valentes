package com.ibmec.mulheresvalentes.model;

public enum StatusInscricao {
    NOVA("Nova", "Aguardando o primeiro contato da equipe"),
    EM_CONTATO("Em contato", "A equipe já está conversando com ela"),
    ACEITA("Aceita", "Cadastrada como participante"),
    ARQUIVADA("Arquivada", "Inscrição encerrada sem cadastro");

    private final String descricao;
    private final String explicacao;

    StatusInscricao(String descricao, String explicacao) {
        this.descricao = descricao;
        this.explicacao = explicacao;
    }

    public String getDescricao() {
        return descricao;
    }

    public String getExplicacao() {
        return explicacao;
    }

    public boolean isAberta() {
        return this == NOVA || this == EM_CONTATO;
    }
}
