package com.ibmec.mulheresvalentes.dto.api;

import com.ibmec.mulheresvalentes.model.Presenca;
import com.ibmec.mulheresvalentes.model.StatusPresenca;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record PresencaResponse(
        Long id,
        Long participanteId,
        String participanteNome,
        Long encontroId,
        Long atividadeId,
        String atividadeNome,
        LocalDate dataEncontro,
        StatusPresenca status,
        String observacoes,
        LocalDateTime registradoEm
) {
    public static PresencaResponse de(Presenca presenca) {
        return new PresencaResponse(
                presenca.getId(),
                presenca.getParticipante().getId(),
                presenca.getParticipante().getNome(),
                presenca.getEncontro().getId(),
                presenca.getEncontro().getAtividade().getId(),
                presenca.getEncontro().getAtividade().getNome(),
                presenca.getEncontro().getData(),
                presenca.getStatus(),
                presenca.getObservacoes(),
                presenca.getRegistradoEm()
        );
    }
}
