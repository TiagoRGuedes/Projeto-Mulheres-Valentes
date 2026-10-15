package com.ibmec.mulheresvalentes.service;

import com.ibmec.mulheresvalentes.dto.FrequenciaParticipanteDto;
import com.ibmec.mulheresvalentes.dto.ItemRegistroPresencaForm;
import com.ibmec.mulheresvalentes.dto.RegistroPresencaForm;
import com.ibmec.mulheresvalentes.dto.ResumoPresencasDto;
import com.ibmec.mulheresvalentes.dto.ResumoRegistroPresencaDto;
import com.ibmec.mulheresvalentes.dto.ResumoFrequenciaDTO;
import com.ibmec.mulheresvalentes.exception.RecursoNaoEncontradoException;
import com.ibmec.mulheresvalentes.exception.RegistroDuplicadoException;
import com.ibmec.mulheresvalentes.exception.RegraNegocioException;
import com.ibmec.mulheresvalentes.model.Encontro;
import com.ibmec.mulheresvalentes.model.Participante;
import com.ibmec.mulheresvalentes.model.Presenca;
import com.ibmec.mulheresvalentes.model.StatusPresenca;
import com.ibmec.mulheresvalentes.model.AcaoHistorico;
import com.ibmec.mulheresvalentes.model.ModuloHistorico;
import com.ibmec.mulheresvalentes.repository.ParticipanteRepository;
import com.ibmec.mulheresvalentes.repository.PresencaRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class PresencaService {

    private static final String MENSAGEM_DUPLICIDADE =
            "Já existe uma presença registrada para este participante nesse encontro.";

    private static final String MENSAGEM_DUPLICIDADE_DATA =
            "Já existe uma presença registrada para este participante nessa data.";

    private final PresencaRepository presencaRepository;
    private final ParticipanteRepository participanteRepository;
    private final ParticipanteService participanteService;
    private final EncontroService encontroService;
    private final HistoricoOperacaoService historicoService;

    public PresencaService(
            PresencaRepository presencaRepository,
            ParticipanteRepository participanteRepository,
            ParticipanteService participanteService,
            EncontroService encontroService,
            HistoricoOperacaoService historicoService
    ) {
        this.presencaRepository = presencaRepository;
        this.participanteRepository = participanteRepository;
        this.participanteService = participanteService;
        this.encontroService = encontroService;
        this.historicoService = historicoService;
    }

    @Transactional(readOnly = true)
    public List<Presenca> listarHistorico(
            Long participanteId,
            Long encontroId,
            Long atividadeId,
            LocalDate dataInicial,
            LocalDate dataFinal,
            StatusPresenca status,
            String busca
    ) {
        validarPeriodo(dataInicial, dataFinal);
        String termo = normalizarBusca(busca);
        return presencaRepository.findAllComRelacionamentos().stream()
                .filter(presenca -> participanteId == null
                        || presenca.getParticipante().getId().equals(participanteId))
                .filter(presenca -> encontroId == null
                        || (presenca.getEncontro() != null
                            && presenca.getEncontro().getId().equals(encontroId)))
                .filter(presenca -> atividadeId == null
                        || (presenca.getEncontro() != null
                            && presenca.getEncontro().getAtividade().getId().equals(atividadeId)))
                .filter(presenca -> dataInicial == null || !presenca.getData().isBefore(dataInicial))
                .filter(presenca -> dataFinal == null || !presenca.getData().isAfter(dataFinal))
                .filter(presenca -> status == null || presenca.getStatus() == status)
                .filter(presenca -> termo == null
                        || presenca.getParticipante().getNome().toLowerCase(Locale.ROOT).contains(termo))
                .toList();
    }

    @Transactional(readOnly = true)
    public Presenca buscar(Long id) {
        if (id == null) {
            throw new RecursoNaoEncontradoException("Registro de presença não encontrado.");
        }
        return presencaRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Registro de presença não encontrado."));
    }

    @Transactional
    public Presenca registrar(
            Long participanteId,
            LocalDate data,
            StatusPresenca status,
            String observacoes
    ) {
        validarStatus(status);
        validarData(data);
        Participante participante = participanteAtivo(participanteId);
        if (presencaRepository.existsByParticipanteIdAndDataAndEncontroIsNull(participanteId, data)) {
            throw new RegistroDuplicadoException(MENSAGEM_DUPLICIDADE_DATA);
        }
        return salvarRegistroIndividual(
                new Presenca(participante, data, status, limparTextoOpcional(observacoes)),
                MENSAGEM_DUPLICIDADE_DATA);
    }

    @Transactional
    public Presenca registrarNoEncontro(
            Long participanteId,
            Long encontroId,
            StatusPresenca status,
            String observacoes
    ) {
        validarStatus(status);
        Participante participante = participanteAtivo(participanteId);
        Encontro encontro = encontroService.buscar(encontroId);
        if (presencaRepository.existsByParticipanteIdAndEncontroId(participanteId, encontroId)) {
            throw new RegistroDuplicadoException(MENSAGEM_DUPLICIDADE);
        }
        return salvarRegistroIndividual(
                new Presenca(participante, encontro, status, limparTextoOpcional(observacoes)),
                MENSAGEM_DUPLICIDADE);
    }

    private Participante participanteAtivo(Long participanteId) {
        Participante participante = participanteService.buscar(participanteId);
        if (!participante.isAtivo()) {
            throw new RegraNegocioException("Ative o participante antes de registrar uma nova presença.");
        }
        return participante;
    }

    private Presenca salvarRegistroIndividual(Presenca presenca, String mensagemDuplicidade) {
        try {
            Presenca salva = presencaRepository.save(presenca);
            presencaRepository.flush();
            historicoService.registrar(
                    ModuloHistorico.PRESENCAS,
                    "Presenca",
                    salva.getId(),
                    salva.getParticipante().getNome(),
                    AcaoHistorico.REGISTRO,
                    "Presença registrada em " + salva.getContexto()
                            + " (" + salva.getData() + ") como "
                            + salva.getStatus().getDescricao().toLowerCase() + ".");
            return salva;
        } catch (DataIntegrityViolationException ex) {
            throw new RegistroDuplicadoException(mensagemDuplicidade);
        }
    }

    @Transactional
    public Presenca corrigir(Long id, StatusPresenca status, String observacoes) {
        validarStatus(status);
        Presenca presenca = buscar(id);
        String observacaoLimpa = limparTextoOpcional(observacoes);
        StatusPresenca statusAnterior = presenca.getStatus();
        String observacaoAnterior = presenca.getObservacoes();
        if (statusAnterior == status && Objects.equals(observacaoAnterior, observacaoLimpa)) {
            return presenca;
        }
        presenca.corrigir(status, observacaoLimpa);
        Presenca salva = presencaRepository.save(presenca);
        historicoService.registrar(
                ModuloHistorico.PRESENCAS,
                "Presenca",
                salva.getId(),
                salva.getParticipante().getNome(),
                AcaoHistorico.CORRECAO,
                "Presença corrigida em " + salva.getContexto()
                        + " (" + salva.getData() + ").",
                "Situação: " + statusAnterior.getDescricao(),
                "Situação: " + salva.getStatus().getDescricao());
        return salva;
    }

    @Transactional(readOnly = true)
    public RegistroPresencaForm prepararRegistroColetivo(LocalDate data, Long encontroId, String busca) {
        LocalDate dataDaChamada = data;
        List<Presenca> jaRegistradas;
        if (encontroId == null) {
            validarData(dataDaChamada);
            jaRegistradas = presencaRepository.findByDataAndEncontroIsNullOrderByParticipanteNomeAsc(dataDaChamada);
        } else {
            Encontro encontro = encontroService.buscar(encontroId);
            dataDaChamada = encontro.getData();
            jaRegistradas = presencaRepository.findByEncontroIdOrderByParticipanteNomeAsc(encontroId);
        }
        Map<Long, Presenca> existentes = jaRegistradas.stream()
                .collect(Collectors.toMap(p -> p.getParticipante().getId(), Function.identity()));

        Map<Long, Participante> participantes = new LinkedHashMap<>();
        participanteRepository.findByAtivoTrueOrderByNomeAsc()
                .forEach(participante -> participantes.put(participante.getId(), participante));
        existentes.values().stream()
                .map(Presenca::getParticipante)
                .forEach(participante -> participantes.putIfAbsent(participante.getId(), participante));

        String termo = normalizarBusca(busca);
        List<ItemRegistroPresencaForm> itens = participantes.values().stream()
                .filter(participante -> termo == null
                        || participante.getNome().toLowerCase(Locale.ROOT).contains(termo))
                .sorted(Comparator.comparing(Participante::getNome, String.CASE_INSENSITIVE_ORDER))
                .map(participante -> {
                    Presenca existente = existentes.get(participante.getId());
                    return new ItemRegistroPresencaForm(
                            participante.getId(),
                            participante.getNome(),
                            existente == null ? null : existente.getStatus(),
                            existente == null ? null : existente.getObservacoes()
                    );
                })
                .toList();
        return new RegistroPresencaForm(dataDaChamada, encontroId, itens);
    }

    @Transactional
    public ResumoRegistroPresencaDto salvarRegistroColetivo(RegistroPresencaForm form) {
        if (form == null) {
            throw new RegraNegocioException("Informe os dados da chamada.");
        }
        Encontro encontro = form.getEncontroId() == null
                ? null
                : encontroService.buscar(form.getEncontroId());
        LocalDate data = encontro == null ? form.getData() : encontro.getData();
        validarData(data);
        List<ItemRegistroPresencaForm> itens = form.getItens();
        if (itens == null || itens.isEmpty()) {
            throw new RegraNegocioException("Não há participantes para registrar.");
        }

        Set<Long> ids = new HashSet<>();
        for (ItemRegistroPresencaForm item : itens) {
            if (item == null || item.getParticipanteId() == null) {
                throw new RegraNegocioException("Não foi possível identificar um dos participantes.");
            }
            if (!ids.add(item.getParticipanteId())) {
                throw new RegraNegocioException("Um participante foi enviado mais de uma vez.");
            }
            validarStatus(item.getStatus());
        }

        Map<Long, Participante> participantes = participanteRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(Participante::getId, Function.identity()));
        if (participantes.size() != ids.size()) {
            throw new RecursoNaoEncontradoException("Um dos participantes não foi encontrado.");
        }

        List<Presenca> jaRegistradas = encontro == null
                ? presencaRepository.findByDataAndEncontroIsNullOrderByParticipanteNomeAsc(data)
                : presencaRepository.findByEncontroIdOrderByParticipanteNomeAsc(encontro.getId());
        Map<Long, Presenca> existentes = jaRegistradas.stream()
                .collect(Collectors.toMap(p -> p.getParticipante().getId(), Function.identity()));
        List<Presenca> paraSalvar = new ArrayList<>(itens.size());
        int novos = 0;
        int corrigidos = 0;

        for (ItemRegistroPresencaForm item : itens) {
            Participante participante = participantes.get(item.getParticipanteId());
            Presenca presenca = existentes.get(item.getParticipanteId());
            String observacaoLimpa = limparTextoOpcional(item.getObservacoes());
            if (presenca == null) {
                if (!participante.isAtivo()) {
                    throw new RegraNegocioException(
                            "O participante " + participante.getNome() + " está inativo e não pode receber um novo registro."
                    );
                }
                presenca = encontro == null
                        ? new Presenca(participante, data, item.getStatus(), observacaoLimpa)
                        : new Presenca(participante, encontro, item.getStatus(), observacaoLimpa);
                paraSalvar.add(presenca);
                novos++;
            } else {
                boolean mudou = presenca.getStatus() != item.getStatus()
                        || !Objects.equals(presenca.getObservacoes(), observacaoLimpa);
                if (mudou) {
                    presenca.corrigir(item.getStatus(), observacaoLimpa);
                    paraSalvar.add(presenca);
                    corrigidos++;
                }
            }
        }

        try {
            if (!paraSalvar.isEmpty()) {
                presencaRepository.saveAll(paraSalvar);
                presencaRepository.flush();
            }
        } catch (DataIntegrityViolationException ex) {
            throw new RegistroDuplicadoException(
                    encontro == null ? MENSAGEM_DUPLICIDADE_DATA : MENSAGEM_DUPLICIDADE);
        }

        String tipoEntidade = encontro == null ? "Chamada" : "Encontro";
        Long entidadeId = encontro == null ? null : encontro.getId();
        String nomeChamada = encontro == null
                ? "Chamada de " + data
                : encontro.getAtividade().getNome() + " — " + encontro.getData();
        if (novos > 0) {
            historicoService.registrar(
                    ModuloHistorico.PRESENCAS,
                    tipoEntidade,
                    entidadeId,
                    nomeChamada,
                    AcaoHistorico.REGISTRO,
                    "Presenças registradas para " + novos
                            + (novos == 1 ? " participante." : " participantes."));
        }
        if (corrigidos > 0) {
            historicoService.registrar(
                    ModuloHistorico.PRESENCAS,
                    tipoEntidade,
                    entidadeId,
                    nomeChamada,
                    AcaoHistorico.CORRECAO,
                    "Presenças corrigidas para " + corrigidos
                            + (corrigidos == 1 ? " participante." : " participantes."));
        }
        return resumirItens(itens);
    }

    @Transactional(readOnly = true)
    public List<FrequenciaParticipanteDto> calcularFrequencias(
            Long atividadeId,
            LocalDate dataInicial,
            LocalDate dataFinal,
            String busca
    ) {
        validarPeriodo(dataInicial, dataFinal);
        List<Presenca> presencas = listarHistorico(
                null, null, atividadeId, dataInicial, dataFinal, null, null
        );
        Map<Long, List<Presenca>> porParticipante = presencas.stream()
                .collect(Collectors.groupingBy(p -> p.getParticipante().getId()));
        String termo = normalizarBusca(busca);

        return participanteRepository.findAllByOrderByNomeAsc().stream()
                .filter(participante -> termo == null
                        || participante.getNome().toLowerCase(Locale.ROOT).contains(termo))
                .map(participante -> calcularFrequencia(
                        participante,
                        porParticipante.getOrDefault(participante.getId(), List.of())
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public ResumoPresencasDto resumir(List<Presenca> presencas) {
        List<Presenca> listaSegura = presencas == null ? List.of() : presencas;
        return new ResumoPresencasDto(
                listaSegura.size(),
                contar(listaSegura, StatusPresenca.PRESENTE),
                contar(listaSegura, StatusPresenca.AUSENTE),
                contar(listaSegura, StatusPresenca.JUSTIFICADA)
        );
    }

    public ResumoFrequenciaDTO resumirFrequencias(List<FrequenciaParticipanteDto> frequencias) {
        List<FrequenciaParticipanteDto> lista = frequencias == null ? List.of() : frequencias;
        long presencas = lista.stream().mapToLong(FrequenciaParticipanteDto::presencas).sum();
        long ausencias = lista.stream().mapToLong(FrequenciaParticipanteDto::ausencias).sum();
        long justificadas = lista.stream().mapToLong(FrequenciaParticipanteDto::justificadas).sum();
        long registros = presencas + ausencias + justificadas;
        BigDecimal media = registros == 0
                ? BigDecimal.ZERO.setScale(1)
                : BigDecimal.valueOf(presencas)
                        .multiply(BigDecimal.valueOf(100))
                        .divide(BigDecimal.valueOf(registros), 1, RoundingMode.HALF_UP);
        return new ResumoFrequenciaDTO(lista.size(), presencas, ausencias, justificadas, media);
    }

    public ResumoRegistroPresencaDto resumirFormulario(RegistroPresencaForm form) {
        if (form == null || form.getItens() == null) {
            return new ResumoRegistroPresencaDto(0, 0, 0, 0);
        }
        return resumirItens(form.getItens());
    }

    private FrequenciaParticipanteDto calcularFrequencia(
            Participante participante,
            List<Presenca> presencas
    ) {
        long presentes = contar(presencas, StatusPresenca.PRESENTE);
        long ausentes = contar(presencas, StatusPresenca.AUSENTE);
        long justificadas = contar(presencas, StatusPresenca.JUSTIFICADA);
        long registros = presencas.size();
        BigDecimal percentual = registros == 0
                ? BigDecimal.ZERO.setScale(1)
                : BigDecimal.valueOf(presentes)
                        .multiply(BigDecimal.valueOf(100))
                        .divide(BigDecimal.valueOf(registros), 1, RoundingMode.HALF_UP);
        return new FrequenciaParticipanteDto(
                participante.getId(), participante.getNome(), registros,
                presentes, ausentes, justificadas, percentual
        );
    }

    private ResumoRegistroPresencaDto resumirItens(List<ItemRegistroPresencaForm> itens) {
        long presentes = itens.stream().filter(item -> item.getStatus() == StatusPresenca.PRESENTE).count();
        long ausentes = itens.stream().filter(item -> item.getStatus() == StatusPresenca.AUSENTE).count();
        long justificadas = itens.stream().filter(item -> item.getStatus() == StatusPresenca.JUSTIFICADA).count();
        return new ResumoRegistroPresencaDto(itens.size(), presentes, ausentes, justificadas);
    }

    private long contar(List<Presenca> presencas, StatusPresenca status) {
        return presencas.stream().filter(presenca -> presenca.getStatus() == status).count();
    }

    private void validarStatus(StatusPresenca status) {
        if (status == null) {
            throw new RegraNegocioException("Marque a situação de todos os participantes.");
        }
    }

    private void validarData(LocalDate data) {
        if (data == null) {
            throw new RegraNegocioException("Informe a data da chamada.");
        }
        if (data.isAfter(LocalDate.now())) {
            throw new RegraNegocioException("A data da presença não pode estar no futuro.");
        }
    }

    private void validarPeriodo(LocalDate dataInicial, LocalDate dataFinal) {
        if (dataInicial != null && dataFinal != null && dataInicial.isAfter(dataFinal)) {
            throw new RegraNegocioException("A data inicial deve ser anterior ou igual à data final.");
        }
    }

    private String normalizarBusca(String busca) {
        if (busca == null || busca.isBlank()) {
            return null;
        }
        return busca.trim().toLowerCase(Locale.ROOT);
    }

    private String limparTextoOpcional(String texto) {
        if (texto == null || texto.isBlank()) {
            return null;
        }
        return texto.trim();
    }
}
