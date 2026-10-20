package com.ibmec.mulheresvalentes.service;

import com.ibmec.mulheresvalentes.dto.ContatoInscricaoForm;
import com.ibmec.mulheresvalentes.dto.ResumoContatosDTO;
import com.ibmec.mulheresvalentes.exception.RegraNegocioException;
import com.ibmec.mulheresvalentes.model.AcaoHistorico;
import com.ibmec.mulheresvalentes.model.ContatoInscricao;
import com.ibmec.mulheresvalentes.model.Inscricao;
import com.ibmec.mulheresvalentes.model.ModuloHistorico;
import com.ibmec.mulheresvalentes.model.StatusInscricao;
import com.ibmec.mulheresvalentes.repository.ContatoInscricaoRepository;
import com.ibmec.mulheresvalentes.repository.InscricaoRepository;
import com.ibmec.mulheresvalentes.security.UsuarioAutenticado;
import com.ibmec.mulheresvalentes.security.UsuarioLogado;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Acompanhamento das inscrições: cada tentativa de contato fica registrada com
 * data, canal, resultado e quem fez. O primeiro contato tira a inscrição de "Nova".
 */
@Service
public class ContatoInscricaoService {

    private final ContatoInscricaoRepository contatoRepository;
    private final InscricaoRepository inscricaoRepository;
    private final InscricaoService inscricaoService;
    private final HistoricoOperacaoService historicoService;

    public ContatoInscricaoService(
            ContatoInscricaoRepository contatoRepository,
            InscricaoRepository inscricaoRepository,
            InscricaoService inscricaoService,
            HistoricoOperacaoService historicoService) {
        this.contatoRepository = contatoRepository;
        this.inscricaoRepository = inscricaoRepository;
        this.inscricaoService = inscricaoService;
        this.historicoService = historicoService;
    }

    @Transactional
    public ContatoInscricao registrar(Long inscricaoId, ContatoInscricaoForm form) {
        Inscricao inscricao = inscricaoService.buscar(inscricaoId);
        if (!inscricao.getStatus().isAberta()) {
            throw new RegraNegocioException(
                    "Esta inscrição já foi " + inscricao.getStatus().getDescricao().toLowerCase()
                            + ". Contatos novos ficam no acompanhamento da participante.");
        }
        if (form == null || form.getCanal() == null || form.getResultado() == null || form.getRealizadoEm() == null) {
            throw new RegraNegocioException("Preencha quando foi, por onde foi e como foi o contato.");
        }
        LocalDateTime quando = form.getRealizadoEm().truncatedTo(ChronoUnit.MINUTES);
        if (quando.isAfter(LocalDateTime.now().plusMinutes(5))) {
            throw new RegraNegocioException("A data do contato não pode estar no futuro.");
        }
        if (quando.isBefore(inscricao.getRecebidaEm().truncatedTo(ChronoUnit.MINUTES))) {
            throw new RegraNegocioException("A data do contato não pode ser anterior ao recebimento da inscrição.");
        }
        Optional<UsuarioAutenticado> autor = UsuarioLogado.atual();
        ContatoInscricao contato = contatoRepository.save(new ContatoInscricao(
                inscricao,
                quando,
                form.getCanal(),
                form.getResultado(),
                limpar(form.getObservacao()),
                autor.map(UsuarioAutenticado::getId).orElse(null),
                autor.map(UsuarioAutenticado::getNome).orElse(null)));

        // O texto da observação não vai para o Histórico: pode conter informação sensível.
        historicoService.registrar(
                ModuloHistorico.INSCRICOES, "Inscricao", inscricao.getId(), inscricao.getNome(),
                AcaoHistorico.REGISTRO,
                "Contato registrado: " + contato.getCanal().getDescricao() + " — " + contato.getResultado().getDescricao() + ".");

        if (inscricao.getStatus() == StatusInscricao.NOVA) {
            inscricaoService.marcarEmContato(inscricao.getId());
        }
        return contato;
    }

    @Transactional(readOnly = true)
    public List<ContatoInscricao> listar(Long inscricaoId) {
        return contatoRepository.findByInscricaoIdOrderByRealizadoEmDescIdDesc(inscricaoId);
    }

    @Transactional(readOnly = true)
    public Map<Long, ResumoContatosDTO> resumirTodas() {
        Map<Long, ResumoContatosDTO> resumo = new HashMap<>();
        for (Object[] linha : contatoRepository.resumirPorInscricao()) {
            resumo.put((Long) linha[0], new ResumoContatosDTO((LocalDateTime) linha[1], (Long) linha[2]));
        }
        return resumo;
    }

    /** Novo formulário já preenchido com a data de agora e o canal que ela preferiu. */
    public ContatoInscricaoForm formularioPara(Inscricao inscricao) {
        ContatoInscricaoForm form = new ContatoInscricaoForm();
        form.setRealizadoEm(LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES));
        form.setCanal(com.ibmec.mulheresvalentes.model.CanalContato.sugeridoPara(inscricao.getPreferenciaContato()));
        return form;
    }

    /** Inscrições novas (sem nenhum contato) recebidas há mais de {@code dias} dias. */
    @Transactional(readOnly = true)
    public List<Inscricao> esperandoPrimeiroContato(int dias) {
        LocalDateTime limite = LocalDateTime.now().minusDays(dias);
        return inscricaoRepository.findByStatusOrderByRecebidaEmDescIdDesc(StatusInscricao.NOVA).stream()
                .filter(inscricao -> inscricao.getRecebidaEm().isBefore(limite))
                .toList();
    }

    private static String limpar(String texto) {
        if (texto == null || texto.isBlank()) {
            return null;
        }
        String limpo = texto.trim();
        return limpo.length() > 1000 ? limpo.substring(0, 1000) : limpo;
    }
}
