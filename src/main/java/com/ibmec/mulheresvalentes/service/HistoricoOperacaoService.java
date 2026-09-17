package com.ibmec.mulheresvalentes.service;

import com.ibmec.mulheresvalentes.dto.GrupoHistoricoDTO;
import com.ibmec.mulheresvalentes.exception.RegraNegocioException;
import com.ibmec.mulheresvalentes.model.AcaoHistorico;
import com.ibmec.mulheresvalentes.model.HistoricoOperacao;
import com.ibmec.mulheresvalentes.model.ModuloHistorico;
import com.ibmec.mulheresvalentes.repository.HistoricoOperacaoRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class HistoricoOperacaoService {

    private static final int TAMANHO_PAGINA = 24;

    private final HistoricoOperacaoRepository historicoRepository;

    public HistoricoOperacaoService(HistoricoOperacaoRepository historicoRepository) {
        this.historicoRepository = historicoRepository;
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public HistoricoOperacao registrar(
            ModuloHistorico modulo,
            String tipoEntidade,
            Long entidadeId,
            String nomeEntidade,
            AcaoHistorico acao,
            String descricao) {
        return registrar(
                modulo, tipoEntidade, entidadeId, nomeEntidade, acao, descricao, null, null);
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public HistoricoOperacao registrar(
            ModuloHistorico modulo,
            String tipoEntidade,
            Long entidadeId,
            String nomeEntidade,
            AcaoHistorico acao,
            String descricao,
            String valorAnteriorResumo,
            String valorNovoResumo) {
        HistoricoOperacao operacao = new HistoricoOperacao(
                modulo,
                limitar(tipoEntidade, 80),
                entidadeId,
                limitar(nomeEntidade, 180),
                acao,
                limitar(descricao, 1000),
                limitar(valorAnteriorResumo, 1000),
                limitar(valorNovoResumo, 1000));
        return historicoRepository.save(operacao);
    }

    @Transactional(readOnly = true)
    public List<HistoricoOperacao> listarRecentes() {
        return historicoRepository.findTop8ByOrderByDataHoraDescIdDesc();
    }

    @Transactional(readOnly = true)
    public List<HistoricoOperacao> listarPorEntidade(String tipoEntidade, Long entidadeId) {
        if (tipoEntidade == null || entidadeId == null) {
            return List.of();
        }
        return historicoRepository.findTop8ByTipoEntidadeAndEntidadeIdOrderByDataHoraDescIdDesc(
                tipoEntidade, entidadeId);
    }

    @Transactional(readOnly = true)
    public Page<HistoricoOperacao> pesquisar(
            LocalDate dataInicial,
            LocalDate dataFinal,
            ModuloHistorico modulo,
            AcaoHistorico acao,
            String busca,
            int pagina) {
        if (dataInicial != null && dataFinal != null && dataInicial.isAfter(dataFinal)) {
            throw new RegraNegocioException("A data inicial deve ser anterior ou igual à data final.");
        }
        LocalDateTime inicio = dataInicial == null ? null : dataInicial.atStartOfDay();
        LocalDateTime fimExclusivo = dataFinal == null ? null : dataFinal.plusDays(1).atStartOfDay();
        String termo = busca == null || busca.isBlank()
                ? null
                : busca.trim().toLowerCase(Locale.ROOT);
        Pageable pageable = PageRequest.of(Math.max(0, pagina), TAMANHO_PAGINA);
        return historicoRepository.pesquisar(inicio, fimExclusivo, modulo, acao, termo, pageable);
    }

    public List<GrupoHistoricoDTO> agruparPorDia(List<HistoricoOperacao> operacoes) {
        Map<LocalDate, List<HistoricoOperacao>> grupos = new LinkedHashMap<>();
        if (operacoes != null) {
            for (HistoricoOperacao operacao : operacoes) {
                grupos.computeIfAbsent(operacao.getDataHora().toLocalDate(), ignorada -> new ArrayList<>())
                        .add(operacao);
            }
        }
        return grupos.entrySet().stream()
                .map(entrada -> new GrupoHistoricoDTO(entrada.getKey(), List.copyOf(entrada.getValue())))
                .toList();
    }

    private String limitar(String valor, int tamanho) {
        if (valor == null) {
            return null;
        }
        String normalizado = valor.trim().replaceAll("\\s+", " ");
        if (normalizado.length() <= tamanho) {
            return normalizado;
        }
        return normalizado.substring(0, tamanho - 1) + "…";
    }
}
