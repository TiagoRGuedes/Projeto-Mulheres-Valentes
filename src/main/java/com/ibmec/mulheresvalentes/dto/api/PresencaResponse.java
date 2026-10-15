package com.ibmec.mulheresvalentes.dto.api;

import com.ibmec.mulheresvalentes.model.Encontro;
import com.ibmec.mulheresvalentes.model.Presenca;
import com.ibmec.mulheresvalentes.model.StatusPresenca;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record PresencaResponse(
        Long id,
        Long participanteId,
        String participanteNome,
        LocalDate data,
        Long encontroId,
        Long atividadeId,
        String atividadeNome,
        StatusPresenca status,
        String observacoes,
        LocalDateTime registradoEm
) {
    public static PresencaResponse de(Presenca presenca) {
        Encontro encontro = presenca.getEncontro();
        return new PresencaResponse(
                presenca.getId(),
                presenca.getParticipante().getId(),
                presenca.getParticipante().getNome(),
                presenca.getData(),
                encontro == null ? null : encontro.getId(),
                encontro == null ? null : encontro.getAtividade().getId(),
                encontro == null ? null : encontro.getAtividade().getNome(),
                presenca.getStatus(),
                presenca.getObservacoes(),
                presenca.getRegistradoEm()
        );
    }
}
