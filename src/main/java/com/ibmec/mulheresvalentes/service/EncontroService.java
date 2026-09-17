package com.ibmec.mulheresvalentes.service;

import com.ibmec.mulheresvalentes.exception.RecursoNaoEncontradoException;
import com.ibmec.mulheresvalentes.exception.RegraNegocioException;
import com.ibmec.mulheresvalentes.dto.ResumoEncontrosDTO;
import com.ibmec.mulheresvalentes.model.AcaoHistorico;
import com.ibmec.mulheresvalentes.model.Atividade;
import com.ibmec.mulheresvalentes.model.Encontro;
import com.ibmec.mulheresvalentes.model.ModuloHistorico;
import com.ibmec.mulheresvalentes.repository.EncontroRepository;
import com.ibmec.mulheresvalentes.repository.PresencaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class EncontroService {

    private final EncontroRepository encontroRepository;
    private final AtividadeService atividadeService;
    private final PresencaRepository presencaRepository;
    private final HistoricoOperacaoService historicoService;

    public EncontroService(
            EncontroRepository encontroRepository,
            AtividadeService atividadeService,
            PresencaRepository presencaRepository,
            HistoricoOperacaoService historicoService) {
        this.encontroRepository = encontroRepository;
        this.atividadeService = atividadeService;
        this.presencaRepository = presencaRepository;
        this.historicoService = historicoService;
    }

    @Transactional(readOnly = true)
    public List<Encontro> listar(Long atividadeId, LocalDate dataInicial, LocalDate dataFinal) {
        validarPeriodo(dataInicial, dataFinal);
        return encontroRepository.findAllByOrderByDataDescIdDesc().stream()
                .filter(encontro -> atividadeId == null || encontro.getAtividade().getId().equals(atividadeId))
                .filter(encontro -> dataInicial == null || !encontro.getData().isBefore(dataInicial))
                .filter(encontro -> dataFinal == null || !encontro.getData().isAfter(dataFinal))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<Encontro> listarDaAtividade(Long atividadeId) {
        if (atividadeId == null) {
            return List.of();
        }
        return encontroRepository.findByAtividadeIdOrderByDataDescIdDesc(atividadeId);
    }

    @Transactional(readOnly = true)
    public Encontro buscar(Long id) {
        if (id == null) {
            throw new RecursoNaoEncontradoException("Encontro não encontrado.");
        }
        return encontroRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Encontro não encontrado."));
    }

    @Transactional
    public Encontro cadastrar(Long atividadeId, LocalDate data, String observacoes) {
        Atividade atividade = atividadeService.buscar(atividadeId);
        if (!atividade.isAtivo()) {
            throw new RegraNegocioException("Escolha uma atividade ativa para criar o encontro.");
        }
        Encontro encontro = new Encontro(atividade, validarData(data), limparTextoOpcional(observacoes));
        Encontro salvo = encontroRepository.save(encontro);
        historicoService.registrar(
                ModuloHistorico.ENCONTROS, "Encontro", salvo.getId(), nomeDoEncontro(salvo),
                AcaoHistorico.CRIACAO, "Encontro cadastrado.", null, resumo(salvo));
        return salvo;
    }

    @Transactional
    public Encontro atualizar(Long id, Long atividadeId, LocalDate data, String observacoes) {
        Encontro encontro = buscar(id);
        String anterior = resumo(encontro);
        Atividade atividade = atividadeService.buscar(atividadeId);
        if (!atividade.isAtivo() && !atividade.getId().equals(encontro.getAtividade().getId())) {
            throw new RegraNegocioException("Escolha uma atividade ativa.");
        }
        encontro.atualizarDados(atividade, validarData(data), limparTextoOpcional(observacoes));
        Encontro salvo = encontroRepository.save(encontro);
        historicoService.registrar(
                ModuloHistorico.ENCONTROS, "Encontro", salvo.getId(), nomeDoEncontro(salvo),
                AcaoHistorico.EDICAO, "Encontro atualizado.", anterior, resumo(salvo));
        return salvo;
    }

    @Transactional
    public void excluir(Long id) {
        Encontro encontro = buscar(id);
        if (presencaRepository.existsByEncontroId(id)) {
            throw new RegraNegocioException(
                    "Este encontro possui registros de presença e não pode ser excluído.");
        }
        historicoService.registrar(
                ModuloHistorico.ENCONTROS, "Encontro", encontro.getId(), nomeDoEncontro(encontro),
                AcaoHistorico.EXCLUSAO, "Encontro excluído por não possuir presenças.",
                resumo(encontro), null);
        encontroRepository.delete(encontro);
    }

    @Transactional(readOnly = true)
    public boolean podeExcluir(Long id) {
        buscar(id);
        return !presencaRepository.existsByEncontroId(id);
    }

    @Transactional(readOnly = true)
    public ResumoEncontrosDTO resumir() {
        LocalDate hoje = LocalDate.now();
        LocalDate inicio = hoje.withDayOfMonth(1);
        LocalDate fim = hoje.withDayOfMonth(hoje.lengthOfMonth());
        return new ResumoEncontrosDTO(
                encontroRepository.count(),
                encontroRepository.countByDataBetween(inicio, fim),
                presencaRepository.contarEncontrosComPresenca());
    }

    private String nomeDoEncontro(Encontro encontro) {
        return encontro.getAtividade().getNome() + " — " + encontro.getData();
    }

    private String resumo(Encontro encontro) {
        return "Atividade: " + encontro.getAtividade().getNome() + "; data: " + encontro.getData();
    }

    private LocalDate validarData(LocalDate data) {
        if (data == null) {
            throw new RegraNegocioException("Informe a data do encontro.");
        }
        return data;
    }

    private void validarPeriodo(LocalDate dataInicial, LocalDate dataFinal) {
        if (dataInicial != null && dataFinal != null && dataInicial.isAfter(dataFinal)) {
            throw new RegraNegocioException("A data inicial deve ser anterior ou igual à data final.");
        }
    }

    private String limparTextoOpcional(String texto) {
        if (texto == null || texto.isBlank()) {
            return null;
        }
        return texto.trim();
    }
}
