package com.ibmec.mulheresvalentes.dto;

import com.ibmec.mulheresvalentes.model.Encontro;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

public class EncontroForm {

    @NotNull(message = "Escolha a atividade.")
    private Long atividadeId;

    @NotNull(message = "Informe a data do encontro.")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate data;

    @Size(max = 1000, message = "As observações devem ter no máximo 1000 caracteres.")
    private String observacoes;

    public EncontroForm() {
    }

    public static EncontroForm de(Encontro encontro) {
        EncontroForm form = new EncontroForm();
        form.setAtividadeId(encontro.getAtividade().getId());
        form.setData(encontro.getData());
        form.setObservacoes(encontro.getObservacoes());
        return form;
    }

    public Long getAtividadeId() {
        return atividadeId;
    }

    public void setAtividadeId(Long atividadeId) {
        this.atividadeId = atividadeId;
    }

    public LocalDate getData() {
        return data;
    }

    public void setData(LocalDate data) {
        this.data = data;
    }

    public String getObservacoes() {
        return observacoes;
    }

    public void setObservacoes(String observacoes) {
        this.observacoes = observacoes;
    }
}
