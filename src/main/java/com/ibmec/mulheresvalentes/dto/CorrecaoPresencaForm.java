package com.ibmec.mulheresvalentes.dto;

import com.ibmec.mulheresvalentes.model.Presenca;
import com.ibmec.mulheresvalentes.model.StatusPresenca;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class CorrecaoPresencaForm {

    @NotNull(message = "Escolha a situação correta.")
    private StatusPresenca status;

    @Size(max = 1000, message = "As observações devem ter no máximo 1000 caracteres.")
    private String observacoes;

    public static CorrecaoPresencaForm de(Presenca presenca) {
        CorrecaoPresencaForm form = new CorrecaoPresencaForm();
        form.setStatus(presenca.getStatus());
        form.setObservacoes(presenca.getObservacoes());
        return form;
    }

    public StatusPresenca getStatus() {
        return status;
    }

    public void setStatus(StatusPresenca status) {
        this.status = status;
    }

    public String getObservacoes() {
        return observacoes;
    }

    public void setObservacoes(String observacoes) {
        this.observacoes = observacoes;
    }
}
