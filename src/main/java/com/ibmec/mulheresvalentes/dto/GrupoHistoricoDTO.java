package com.ibmec.mulheresvalentes.dto;

import com.ibmec.mulheresvalentes.model.HistoricoOperacao;

import java.time.LocalDate;
import java.util.List;

public record GrupoHistoricoDTO(LocalDate data, List<HistoricoOperacao> operacoes) {
}
