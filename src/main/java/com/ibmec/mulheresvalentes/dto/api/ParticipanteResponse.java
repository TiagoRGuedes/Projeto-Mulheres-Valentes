package com.ibmec.mulheresvalentes.dto.api;

import com.ibmec.mulheresvalentes.model.Participante;

import java.time.LocalDateTime;

public record ParticipanteResponse(
        Long id,
        String nome,
        String contato,
        boolean ativo,
        String observacoes,
        LocalDateTime dataCadastro,
        LocalDateTime dataAtualizacao
) {
    public static ParticipanteResponse de(Participante participante) {
        return new ParticipanteResponse(
                participante.getId(),
                participante.getNome(),
                participante.getContato(),
                participante.isAtivo(),
                participante.getObservacoes(),
                participante.getDataCadastro(),
                participante.getDataAtualizacao()
        );
    }
}
