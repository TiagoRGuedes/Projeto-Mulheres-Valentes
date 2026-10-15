package com.ibmec.mulheresvalentes.dto;

import com.ibmec.mulheresvalentes.model.Presenca;

import java.util.List;

public record ParticipanteConectadoDTO(
        FrequenciaParticipanteDto frequencia,
        List<Presenca> presencasRecentes
) {
}
