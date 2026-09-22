package com.ibmec.mulheresvalentes.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class RegistroPresencaForm {

    @NotNull(message = "Informe a data da chamada.")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate data;

    private Long encontroId;

    @Size(min = 1, message = "Não há participantes para registrar.")
    private List<@Valid ItemRegistroPresencaForm> itens = new ArrayList<>();

    public RegistroPresencaForm() {
        this.data = LocalDate.now();
    }

    public RegistroPresencaForm(LocalDate data, Long encontroId, List<ItemRegistroPresencaForm> itens) {
        this.data = data;
        this.encontroId = encontroId;
        this.itens = new ArrayList<>(itens);
    }

    public void marcarTodosComoPresentes() {
        itens.forEach(item -> item.setStatus(com.ibmec.mulheresvalentes.model.StatusPresenca.PRESENTE));
    }

    public boolean isVinculadoAEncontro() {
        return encontroId != null;
    }

    public LocalDate getData() {
        return data;
    }

    public void setData(LocalDate data) {
        this.data = data;
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
