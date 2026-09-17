package com.ibmec.mulheresvalentes.dto;

import com.ibmec.mulheresvalentes.model.StatusPresenca;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class ItemRegistroPresencaForm {

    @NotNull(message = "Não foi possível identificar um dos participantes.")
    private Long participanteId;

    private String participanteNome;

    @NotNull(message = "Marque a situação de todos os participantes.")
    private StatusPresenca status;

    @Size(max = 1000, message = "As observações devem ter no máximo 1000 caracteres.")
    private String observacoes;

    public ItemRegistroPresencaForm() {
    }

    public ItemRegistroPresencaForm(
            Long participanteId,
            String participanteNome,
            StatusPresenca status,
            String observacoes
    ) {
        this.participanteId = participanteId;
        this.participanteNome = participanteNome;
        this.status = status;
        this.observacoes = observacoes;
    }

    public Long getParticipanteId() {
        return participanteId;
    }

    public void setParticipanteId(Long participanteId) {
        this.participanteId = participanteId;
    }

    public String getParticipanteNome() {
        return participanteNome;
    }

    public void setParticipanteNome(String participanteNome) {
        this.participanteNome = participanteNome;
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
