package com.ibmec.mulheresvalentes.dto;

import com.ibmec.mulheresvalentes.model.Atividade;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class AtividadeForm {

    @NotBlank(message = "Informe o nome da atividade.")
    @Size(max = 150, message = "O nome deve ter no máximo 150 caracteres.")
    private String nome;

    @Size(max = 1000, message = "A descrição deve ter no máximo 1000 caracteres.")
    private String descricao;

    public AtividadeForm() {
    }

    public static AtividadeForm de(Atividade atividade) {
        AtividadeForm form = new AtividadeForm();
        form.setNome(atividade.getNome());
        form.setDescricao(atividade.getDescricao());
        return form;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }
}
