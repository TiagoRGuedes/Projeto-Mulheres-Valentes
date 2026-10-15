package com.ibmec.mulheresvalentes.service;

import com.ibmec.mulheresvalentes.dto.InscricaoForm;
import com.ibmec.mulheresvalentes.dto.ResumoInscricoesDTO;
import com.ibmec.mulheresvalentes.exception.RecursoNaoEncontradoException;
import com.ibmec.mulheresvalentes.exception.RegraNegocioException;
import com.ibmec.mulheresvalentes.model.AcaoHistorico;
import com.ibmec.mulheresvalentes.model.Inscricao;
import com.ibmec.mulheresvalentes.model.ModuloHistorico;
import com.ibmec.mulheresvalentes.model.Participante;
import com.ibmec.mulheresvalentes.model.StatusInscricao;
import com.ibmec.mulheresvalentes.repository.InscricaoRepository;
import com.ibmec.mulheresvalentes.repository.ParticipanteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
public class InscricaoService {

    private static final int TAMANHO_MINIMO_CONTATO_COMPARAVEL = 8;

    private final InscricaoRepository inscricaoRepository;
    private final ParticipanteRepository participanteRepository;
    private final ParticipanteService participanteService;
    private final HistoricoOperacaoService historicoService;
    private final FormatadorService formatador;

    public InscricaoService(
            InscricaoRepository inscricaoRepository,
            ParticipanteRepository participanteRepository,
            ParticipanteService participanteService,
            HistoricoOperacaoService historicoService,
            FormatadorService formatador) {
        this.inscricaoRepository = inscricaoRepository;
        this.participanteRepository = participanteRepository;
        this.participanteService = participanteService;
        this.historicoService = historicoService;
        this.formatador = formatador;
    }

    @Transactional
    public Inscricao receber(InscricaoForm form) {
        if (form == null) {
            throw new RegraNegocioException("Preencha o formulário de inscrição.");
        }
        if (!form.isConsentimento()) {
            throw new RegraNegocioException(
                    "Para enviar, confirme que autoriza a ONG a guardar seus dados e entrar em contato.");
        }
        if (form.getPreferenciaContato() == null) {
            throw new RegraNegocioException("Escolha como prefere que a gente fale com você.");
        }
        Inscricao inscricao = new Inscricao(
                obrigatorio(form.getNome(), 150, "Escreva o seu nome."),
                obrigatorio(form.getContato(), 120,
                        "Deixe um telefone, WhatsApp ou e-mail para a gente falar com você."),
                form.getPreferenciaContato(),
                opcional(form.getContatoSeguro(), 200),
                opcional(form.getBairro(), 100),
                form.getAreas(),
                opcional(form.getMensagem(), 1000),
                true);
        Inscricao salva = inscricaoRepository.save(inscricao);
        historicoService.registrar(
                ModuloHistorico.INSCRICOES, "Inscricao", salva.getId(), salva.getNome(),
                AcaoHistorico.REGISTRO, "Inscrição recebida pela página pública.");
        return salva;
    }

    @Transactional(readOnly = true)
    public List<Inscricao> listar(StatusInscricao status) {
        return status == null
                ? inscricaoRepository.findAllByOrderByRecebidaEmDescIdDesc()
                : inscricaoRepository.findByStatusOrderByRecebidaEmDescIdDesc(status);
    }

    @Transactional(readOnly = true)
    public Inscricao buscar(Long id) {
        if (id == null) {
            throw new RecursoNaoEncontradoException("Inscrição não encontrada.");
        }
        return inscricaoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Inscrição não encontrada."));
    }

    @Transactional(readOnly = true)
    public ResumoInscricoesDTO resumir() {
        return new ResumoInscricoesDTO(
                inscricaoRepository.countByStatus(StatusInscricao.NOVA),
                inscricaoRepository.countByStatus(StatusInscricao.EM_CONTATO),
                inscricaoRepository.countByStatus(StatusInscricao.ACEITA),
                inscricaoRepository.countByStatus(StatusInscricao.ARQUIVADA));
    }

    @Transactional(readOnly = true)
    public long contarNovas() {
        return inscricaoRepository.countByStatus(StatusInscricao.NOVA);
    }

    @Transactional(readOnly = true)
    public Optional<Participante> participanteComMesmoContato(Inscricao inscricao) {
        String procurado = normalizarContato(inscricao.getContato());
        if (procurado.length() < TAMANHO_MINIMO_CONTATO_COMPARAVEL) {
            return Optional.empty();
        }
        return participanteRepository.findAllByOrderByNomeAsc().stream()
                .filter(participante -> procurado.equals(normalizarContato(participante.getContato())))
                .findFirst();
    }

    @Transactional
    public Inscricao marcarEmContato(Long id) {
        Inscricao inscricao = buscar(id);
        if (inscricao.getStatus() != StatusInscricao.NOVA) {
            throw new RegraNegocioException("Somente uma inscrição nova pode ser marcada como em contato.");
        }
        inscricao.marcarEmContato();
        Inscricao salva = inscricaoRepository.save(inscricao);
        historicoService.registrar(
                ModuloHistorico.INSCRICOES, "Inscricao", salva.getId(), salva.getNome(),
                AcaoHistorico.EDICAO, "A equipe iniciou o contato com a inscrita.",
                "Situação: " + StatusInscricao.NOVA.getDescricao(),
                "Situação: " + salva.getStatus().getDescricao());
        return salva;
    }

    @Transactional
    public Inscricao aceitar(Long id) {
        Inscricao inscricao = buscar(id);
        exigirAberta(inscricao);
        Optional<Participante> existente = participanteComMesmoContato(inscricao);
        Participante participante;
        String descricao;
        if (existente.isPresent()) {
            participante = existente.get();
            if (!participante.isAtivo()) {
                participante = participanteService.ativar(participante.getId());
            }
            descricao = "Inscrição aceita e vinculada ao cadastro já existente de "
                    + participante.getNome() + ".";
        } else {
            participante = participanteService.cadastrar(
                    inscricao.getNome(),
                    inscricao.getContato(),
                    "Inscrita pela página pública em "
                            + formatador.data(inscricao.getRecebidaEm().toLocalDate()) + ".");
            descricao = "Inscrição aceita; participante cadastrada.";
        }
        StatusInscricao anterior = inscricao.getStatus();
        inscricao.aceitar(participante);
        Inscricao salva = inscricaoRepository.save(inscricao);
        historicoService.registrar(
                ModuloHistorico.INSCRICOES, "Inscricao", salva.getId(), salva.getNome(),
                AcaoHistorico.ATIVACAO, descricao,
                "Situação: " + anterior.getDescricao(),
                "Situação: " + salva.getStatus().getDescricao());
        return salva;
    }

    @Transactional
    public Inscricao arquivar(Long id) {
        Inscricao inscricao = buscar(id);
        exigirAberta(inscricao);
        StatusInscricao anterior = inscricao.getStatus();
        inscricao.arquivar();
        Inscricao salva = inscricaoRepository.save(inscricao);
        historicoService.registrar(
                ModuloHistorico.INSCRICOES, "Inscricao", salva.getId(), salva.getNome(),
                AcaoHistorico.DESATIVACAO, "Inscrição arquivada sem cadastro de participante.",
                "Situação: " + anterior.getDescricao(),
                "Situação: " + salva.getStatus().getDescricao());
        return salva;
    }

    static String normalizarContato(String contato) {
        if (!StringUtils.hasText(contato)) {
            return "";
        }
        String limpo = contato.trim().toLowerCase(Locale.ROOT);
        if (limpo.contains("@")) {
            return limpo.replaceAll("\\s+", "");
        }
        return limpo.replaceAll("\\D", "");
    }

    private void exigirAberta(Inscricao inscricao) {
        if (!inscricao.getStatus().isAberta()) {
            throw new RegraNegocioException(
                    "Esta inscrição já foi " + inscricao.getStatus().getDescricao().toLowerCase(Locale.ROOT) + ".");
        }
    }

    private String obrigatorio(String valor, int limite, String mensagem) {
        if (!StringUtils.hasText(valor)) {
            throw new RegraNegocioException(mensagem);
        }
        String limpo = valor.trim().replaceAll("\\s+", " ");
        if (limpo.length() > limite) {
            throw new RegraNegocioException("Use no máximo " + limite + " caracteres.");
        }
        return limpo;
    }

    private String opcional(String valor, int limite) {
        if (!StringUtils.hasText(valor)) {
            return null;
        }
        String limpo = valor.trim();
        if (limpo.length() > limite) {
            throw new RegraNegocioException("Use no máximo " + limite + " caracteres.");
        }
        return limpo;
    }
}
