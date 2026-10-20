package com.ibmec.mulheresvalentes.dto;

import java.time.LocalDateTime;

/** Último contato e quantidade de contatos de uma inscrição, para a lista. */
public record ResumoContatosDTO(LocalDateTime ultimoContato, long quantidade) {
}
