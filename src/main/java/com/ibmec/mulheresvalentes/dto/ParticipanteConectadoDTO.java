package com.ibmec.mulheresvalentes.dto;

import com.ibmec.mulheresvalentes.model.Presenca;

import java.util.List;

/**
 * Visão consolidada usada na página de uma pessoa. Mantém os cálculos fora do
 * controller e deixa explícita a ligação entre cadastro, presença e frequência.
 */
public record ParticipanteConectadoDTO(
        FrequenciaParticipanteDto frequencia,
        List<Presenca> presencasRecentes
) {
}
