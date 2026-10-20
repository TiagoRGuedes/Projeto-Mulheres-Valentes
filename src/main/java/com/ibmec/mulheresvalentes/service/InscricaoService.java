package com.ibmec.mulheresvalentes.service;

import com.ibmec.mulheresvalentes.dto.InscricaoForm;
import com.ibmec.mulheresvalentes.dto.ResumoInscricoesDTO;
import com.ibmec.mulheresvalentes.exception.RecursoNaoEncontradoException;
import com.ibmec.mulheresvalentes.exception.RegraNegocioException;
import com.ibmec.mulheresvalentes.model.AcaoHistorico;
import com.ibmec.mulheresvalentes.model.Inscricao;
import com.ibmec.mulheresvalentes.model.ModuloHistorico;
import com.ibmec.mulheresvalentes.model.Participante;
import com.ibmec.mulheresvalentes.model.PreferenciaContato;
import com.ibmec.mulheresvalentes.model.StatusInscricao;
import com.ibmec.mulheresvalentes.repository.InscricaoRepository;
import com.ibmec.mulheresvalentes.repository.ParticipanteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Pattern;

@Service
public class InscricaoService {

    private static final int TAMANHO_MINIMO_CONTATO_COMPARAVEL = 8;
    private static final Pattern EMAIL = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]{2,}$");
    private static final Pattern CARACTERES_DE_TELEFONE = Pattern.compile("^[0-9()+.\\-\\s]+$");

    private final InscricaoRepository inscricaoRepository;
    private final ParticipanteRepository participanteRepository;
    private final ParticipanteService participanteService;
    private final HistoricoOperacaoService historicoService;
    private final FormatadorService formatador;
    private final NotificacaoService notificacaoService;

    public InscricaoService(
            InscricaoRepository inscricaoRepository,
            ParticipanteRepository participanteRepository,
            ParticipanteService participanteService,
            HistoricoOperacaoService historicoService,
            FormatadorService formatador,
            NotificacaoService notificacaoService) {
        this.inscricaoRepository = inscricaoRepository;
        this.participanteRepository = participanteRepository;
        this.participanteService = participanteService;
        this.historicoService = historicoService;
        this.formatador = formatador;
        this.notificacaoService = notificacaoService;
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
        String contato = obrigatorio(form.getContato(), 120,
                "Deixe um telefone, WhatsApp ou e-mail para a gente falar com você.");
        problemaNoContato(contato).ifPresent(problema -> {
            throw new RegraNegocioException(problema);
        });
        problemaNaPreferencia(contato, form.getPreferenciaContato()).ifPresent(problema -> {
            throw new RegraNegocioException(problema);
        });
        Inscricao inscricao = new Inscricao(
                obrigatorio(form.getNome(), 150, "Escreva o seu nome."),
                formatarContato(contato),
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
        notificacaoService.notificarNovaInscricao(salva);
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

    public static Optional<String> problemaNoContato(String contato) {
        if (!StringUtils.hasText(contato)) {
            return Optional.empty();
        }
        String limpo = contato.trim();
        if (limpo.contains("@")) {
            return EMAIL.matcher(limpo).matches()
                    ? Optional.empty()
                    : Optional.of("Este e-mail parece incompleto. Confira se está no formato nome@exemplo.com.");
        }
        if (!CARACTERES_DE_TELEFONE.matcher(limpo).matches()) {
            return Optional.of("Escreva um telefone só com números, ou um e-mail completo.");
        }
        int digitos = digitosDoTelefone(limpo).length();
        if (digitos == 8 || digitos == 9) {
            return Optional.of("Parece faltar o DDD ou algum número. Confira: DDD e número, por exemplo (11) 91234-5678.");
        }
        if (digitos != 10 && digitos != 11) {
            return Optional.of("Este telefone parece ter números a mais ou a menos. Confira: DDD e número, por exemplo (11) 91234-5678.");
        }
        return Optional.empty();
    }

    public static Optional<String> problemaNaPreferencia(String contato, PreferenciaContato preferencia) {
        if (!StringUtils.hasText(contato) || preferencia == null || problemaNoContato(contato).isPresent()) {
            return Optional.empty();
        }
        boolean email = contato.contains("@");
        if (preferencia == PreferenciaContato.EMAIL && !email) {
            return Optional.of("Você escolheu e-mail, mas deixou um telefone. Troque a opção ou informe um e-mail.");
        }
        if (preferencia != PreferenciaContato.EMAIL && email) {
            return Optional.of("Você deixou um e-mail. Para ligação ou WhatsApp, informe um telefone, ou escolha a opção e-mail.");
        }
        return Optional.empty();
    }

    public static String formatarContato(String contato) {
        if (!StringUtils.hasText(contato)) {
            return contato;
        }
        String limpo = contato.trim();
        if (limpo.contains("@")) {
            return limpo.toLowerCase(Locale.ROOT);
        }
        String digitos = digitosDoTelefone(limpo);
        if (digitos.length() == 11) {
            return "(" + digitos.substring(0, 2) + ") " + digitos.substring(2, 7) + "-" + digitos.substring(7);
        }
        if (digitos.length() == 10) {
            return "(" + digitos.substring(0, 2) + ") " + digitos.substring(2, 6) + "-" + digitos.substring(6);
        }
        return limpo;
    }

    private static String digitosDoTelefone(String telefone) {
        String digitos = telefone.replaceAll("\\D", "");
        if (digitos.startsWith("55") && (digitos.length() == 12 || digitos.length() == 13)) {
            return digitos.substring(2);
        }
        return digitos;
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
