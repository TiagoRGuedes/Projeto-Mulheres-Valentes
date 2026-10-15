package com.ibmec.mulheresvalentes.dto;

import com.ibmec.mulheresvalentes.model.Participante;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ParticipanteForm {

    @NotBlank(message = "Informe o nome do participante.")
    @Size(max = 150, message = "O nome deve ter no máximo 150 caracteres.")
    private String nome;

    @Size(max = 120, message = "O contato deve ter no máximo 120 caracteres.")
    private String contato;

    @Size(max = 1000, message = "As observações devem ter no máximo 1000 caracteres.")
    private String observacoes;

    public ParticipanteForm() {
    }

    public static ParticipanteForm de(Participante participante) {
        ParticipanteForm form = new ParticipanteForm();
        form.setNome(participante.getNome());
        form.setContato(participante.getContato());
        form.setObservacoes(participante.getObservacoes());
        return form;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getContato() {
        return contato;
    }

    public void setContato(String contato) {
        this.contato = contato;
    }

    public String getObservacoes() {
        return observacoes;
    }

    public void setObservacoes(String observacoes) {
        this.observacoes = observacoes;
    }
}
