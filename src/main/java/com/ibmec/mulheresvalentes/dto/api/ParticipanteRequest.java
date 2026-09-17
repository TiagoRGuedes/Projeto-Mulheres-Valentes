package com.ibmec.mulheresvalentes.dto.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ParticipanteRequest(
        @NotBlank(message = "Informe o nome do participante.")
        @Size(max = 150, message = "O nome deve ter no máximo 150 caracteres.")
        String nome,

        @Size(max = 1000, message = "As observações devem ter no máximo 1000 caracteres.")
        String observacoes
) {
}
