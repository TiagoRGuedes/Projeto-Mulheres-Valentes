package com.ibmec.mulheresvalentes.model;

public enum AreaApoio {
    ACOLHIMENTO("Acolhimento e rodas de conversa"),
    SAUDE_EMOCIONAL("Psicoterapia e saúde emocional"),
    ORIENTACAO_JURIDICA("Orientação jurídica"),
    PREVENCAO_SAUDE("Prevenção e saúde"),
    CURSOS_RENDA("Cursos e geração de renda"),
    REFORCO_ESCOLAR("Reforço escolar para meus filhos (6 a 14 anos)"),
    CULTURA("Atividades culturais"),
    QUERO_CONVERSAR("Ainda não sei, quero conversar");

    private final String descricao;

    AreaApoio(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
