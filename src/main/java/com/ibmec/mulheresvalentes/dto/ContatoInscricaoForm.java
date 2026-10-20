package com.ibmec.mulheresvalentes.dto;

import com.ibmec.mulheresvalentes.model.CanalContato;
import com.ibmec.mulheresvalentes.model.ResultadoContato;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

public class ContatoInscricaoForm {

    @NotNull(message = "Informe quando foi o contato.")
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime realizadoEm;

    @NotNull(message = "Escolha por onde foi o contato.")
    private CanalContato canal;

    @NotNull(message = "Escolha como foi o contato.")
    private ResultadoContato resultado;

    @Size(max = 1000, message = "Use no máximo 1000 caracteres.")
    private String observacao;

    public LocalDateTime getRealizadoEm() {
        return realizadoEm;
    }

    public void setRealizadoEm(LocalDateTime realizadoEm) {
        this.realizadoEm = realizadoEm;
    }

    public CanalContato getCanal() {
        return canal;
    }

    public void setCanal(CanalContato canal) {
        this.canal = canal;
    }

    public ResultadoContato getResultado() {
        return resultado;
    }

    public void setResultado(ResultadoContato resultado) {
        this.resultado = resultado;
    }

    public String getObservacao() {
        return observacao;
    }

    public void setObservacao(String observacao) {
        this.observacao = observacao;
    }
}
