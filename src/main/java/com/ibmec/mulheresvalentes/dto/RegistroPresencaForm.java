package com.ibmec.mulheresvalentes.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.ArrayList;
import java.util.List;

public class RegistroPresencaForm {

    @NotNull(message = "Escolha o encontro.")
    private Long encontroId;

    @Size(min = 1, message = "Não há participantes para registrar.")
    private List<@Valid ItemRegistroPresencaForm> itens = new ArrayList<>();

    public RegistroPresencaForm() {
    }

    public RegistroPresencaForm(Long encontroId, List<ItemRegistroPresencaForm> itens) {
        this.encontroId = encontroId;
        this.itens = new ArrayList<>(itens);
    }

    public void marcarTodosComoPresentes() {
        itens.forEach(item -> item.setStatus(com.ibmec.mulheresvalentes.model.StatusPresenca.PRESENTE));
    }

    public Long getEncontroId() {
        return encontroId;
    }

    public void setEncontroId(Long encontroId) {
        this.encontroId = encontroId;
    }

    public List<ItemRegistroPresencaForm> getItens() {
        return itens;
    }

    public void setItens(List<ItemRegistroPresencaForm> itens) {
        this.itens = itens == null ? new ArrayList<>() : new ArrayList<>(itens);
    }
}
