package com.ibmec.mulheresvalentes.service;

import com.ibmec.mulheresvalentes.exception.RecursoNaoEncontradoException;
import com.ibmec.mulheresvalentes.exception.RegraNegocioException;
import com.ibmec.mulheresvalentes.dto.ResumoParticipantesDTO;
import com.ibmec.mulheresvalentes.model.AcaoHistorico;
import com.ibmec.mulheresvalentes.model.ModuloHistorico;
import com.ibmec.mulheresvalentes.model.Participante;
import com.ibmec.mulheresvalentes.repository.ParticipanteRepository;
import com.ibmec.mulheresvalentes.repository.PresencaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.time.LocalDateTime;

@Service
public class ParticipanteService {

    private final ParticipanteRepository participanteRepository;
    private final PresencaRepository presencaRepository;
    private final HistoricoOperacaoService historicoService;

    public ParticipanteService(
            ParticipanteRepository participanteRepository,
            PresencaRepository presencaRepository,
            HistoricoOperacaoService historicoService) {
        this.participanteRepository = participanteRepository;
        this.presencaRepository = presencaRepository;
        this.historicoService = historicoService;
    }

    @Transactional(readOnly = true)
    public List<Participante> listar(String busca) {
        if (busca == null || busca.isBlank()) {
            return participanteRepository.findAllByOrderByNomeAsc();
        }
        return participanteRepository.pesquisar(busca.trim());
    }

    @Transactional(readOnly = true)
    public List<Participante> listarAtivos() {
        return participanteRepository.findByAtivoTrueOrderByNomeAsc();
    }

    @Transactional(readOnly = true)
    public Participante buscar(Long id) {
        if (id == null) {
            throw new RecursoNaoEncontradoException("Participante não encontrado.");
        }
        return participanteRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Participante não encontrado."));
    }

    @Transactional
    public Participante cadastrar(String nome, String contato, String observacoes) {
        Participante participante = new Participante(
                validarNome(nome), validarContato(contato), limparTextoOpcional(observacoes));
        Participante salvo = participanteRepository.save(participante);
        historicoService.registrar(
                ModuloHistorico.PARTICIPANTES, "Participante", salvo.getId(), salvo.getNome(),
                AcaoHistorico.CRIACAO, "Participante cadastrado.");
        return salvo;
    }

    @Transactional
    public Participante atualizar(Long id, String nome, String contato, String observacoes) {
        Participante participante = buscar(id);
        String anterior = resumo(participante);
        participante.atualizarDados(
                validarNome(nome), validarContato(contato), limparTextoOpcional(observacoes));
        Participante salvo = participanteRepository.save(participante);
        historicoService.registrar(
                ModuloHistorico.PARTICIPANTES, "Participante", salvo.getId(), salvo.getNome(),
                AcaoHistorico.EDICAO, "Cadastro do participante atualizado.",
                anterior, resumo(salvo));
        return salvo;
    }

    @Transactional
    public Participante ativar(Long id) {
        Participante participante = buscar(id);
        if (participante.isAtivo()) {
            return participante;
        }
        participante.ativar();
        Participante salvo = participanteRepository.save(participante);
        historicoService.registrar(
                ModuloHistorico.PARTICIPANTES, "Participante", salvo.getId(), salvo.getNome(),
                AcaoHistorico.ATIVACAO, "Participante ativado para novos registros de presença.");
        return salvo;
    }

    @Transactional
    public Participante desativar(Long id) {
        Participante participante = buscar(id);
        if (!participante.isAtivo()) {
            return participante;
        }
        participante.desativar();
        Participante salvo = participanteRepository.save(participante);
        historicoService.registrar(
                ModuloHistorico.PARTICIPANTES, "Participante", salvo.getId(), salvo.getNome(),
                AcaoHistorico.DESATIVACAO,
                "Participante desativado. Os registros anteriores foram mantidos.");
        return salvo;
    }

    @Transactional
    public void excluir(Long id) {
        Participante participante = buscar(id);
        if (presencaRepository.existsByParticipanteId(id)) {
            throw new RegraNegocioException(
                    "Este participante possui registros de presença e não pode ser excluído. "
                            + "Você pode desativá-lo.");
        }
        historicoService.registrar(
                ModuloHistorico.PARTICIPANTES, "Participante", participante.getId(), participante.getNome(),
                AcaoHistorico.EXCLUSAO, "Cadastro de participante excluído por não possuir presenças.");
        participanteRepository.delete(participante);
    }

    @Transactional(readOnly = true)
    public boolean podeExcluir(Long id) {
        buscar(id);
        return !presencaRepository.existsByParticipanteId(id);
    }

    @Transactional(readOnly = true)
    public ResumoParticipantesDTO resumir() {
        long total = participanteRepository.count();
        long ativos = participanteRepository.countByAtivoTrue();
        return new ResumoParticipantesDTO(
                total,
                ativos,
                participanteRepository.countByAtivoFalse(),
                participanteRepository.countByDataCadastroGreaterThanEqual(LocalDateTime.now().minusDays(30)));
    }

    private String validarNome(String nome) {
        if (nome == null || nome.isBlank()) {
            throw new RegraNegocioException("Informe o nome do participante.");
        }
        String nomeLimpo = nome.trim();
        if (nomeLimpo.length() > 150) {
            throw new RegraNegocioException("O nome deve ter no máximo 150 caracteres.");
        }
        return nomeLimpo;
    }

    private String validarContato(String contato) {
        String contatoLimpo = limparTextoOpcional(contato);
        if (contatoLimpo != null && contatoLimpo.length() > 120) {
            throw new RegraNegocioException("O contato deve ter no máximo 120 caracteres.");
        }
        return contatoLimpo;
    }

    private String resumo(Participante participante) {
        return "Nome: " + participante.getNome()
                + "; contato: " + (participante.getContato() == null
                        ? "não informado" : participante.getContato());
    }

    private String limparTextoOpcional(String texto) {
        if (texto == null || texto.isBlank()) {
            return null;
        }
        return texto.trim();
    }
}
