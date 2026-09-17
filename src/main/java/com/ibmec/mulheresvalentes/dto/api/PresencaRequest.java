package com.ibmec.mulheresvalentes.dto.api;

import com.ibmec.mulheresvalentes.model.StatusPresenca;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record PresencaRequest(
        @NotNull(message = "Escolha o participante.") Long participanteId,
        @NotNull(message = "Escolha o encontro.") Long encontroId,
        @NotNull(message = "Informe a situação da presença.") StatusPresenca status,
        @Size(max = 1000, message = "As observações devem ter no máximo 1000 caracteres.") String observacoes
) {
}
