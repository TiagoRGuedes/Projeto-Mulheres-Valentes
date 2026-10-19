package com.ibmec.mulheresvalentes.model;

public enum PerfilUsuario {
    ADMIN("Administrador(a)", "Acesso completo, incluindo cadastro de usuários, histórico do sistema, categorias e exclusões."),
    OPERADOR("Operador(a)", "Trabalho do dia a dia: inscrições, participantes, presença, lançamentos do caixa e relatórios.");

    private final String descricao;
    private final String explicacao;

    PerfilUsuario(String descricao, String explicacao) {
        this.descricao = descricao;
        this.explicacao = explicacao;
    }

    public String getDescricao() {
        return descricao;
    }

    public String getExplicacao() {
        return explicacao;
    }

    /** Nome da autoridade usada pelo Spring Security (ex.: ROLE_ADMIN). */
    public String getAutoridade() {
        return "ROLE_" + name();
    }
}
