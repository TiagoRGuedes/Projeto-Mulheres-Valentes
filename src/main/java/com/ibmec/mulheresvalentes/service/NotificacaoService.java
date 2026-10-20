package com.ibmec.mulheresvalentes.service;

import com.ibmec.mulheresvalentes.model.Inscricao;
import com.ibmec.mulheresvalentes.model.Notificacao;
import com.ibmec.mulheresvalentes.model.StatusInscricao;
import com.ibmec.mulheresvalentes.model.TipoNotificacao;
import com.ibmec.mulheresvalentes.model.Usuario;
import com.ibmec.mulheresvalentes.repository.InscricaoRepository;
import com.ibmec.mulheresvalentes.repository.NotificacaoRepository;
import com.ibmec.mulheresvalentes.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Avisos para a equipe no painel. Cada pessoa tem o seu "visto até":
 * o que chegou depois disso aparece como novo no sino.
 *
 * Por segurança, a notificação mostra só o primeiro nome e o bairro;
 * o restante fica na página da inscrição, que exige login.
 */
@Service
public class NotificacaoService {

    private final NotificacaoRepository notificacaoRepository;
    private final InscricaoRepository inscricaoRepository;
    private final UsuarioRepository usuarioRepository;

    public NotificacaoService(
            NotificacaoRepository notificacaoRepository,
            InscricaoRepository inscricaoRepository,
            UsuarioRepository usuarioRepository) {
        this.notificacaoRepository = notificacaoRepository;
        this.inscricaoRepository = inscricaoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional
    public Notificacao notificarNovaInscricao(Inscricao inscricao) {
        String mensagem = primeiroNome(inscricao.getNome())
                + (inscricao.getBairro() != null ? ", de " + inscricao.getBairro() + "," : "")
                + " se inscreveu pelo site e espera o primeiro contato.";
        return notificacaoRepository.save(new Notificacao(
                TipoNotificacao.NOVA_INSCRICAO,
                "Nova inscrição pelo site",
                mensagem,
                "/inscricoes/" + inscricao.getId(),
                inscricao.getId()));
    }

    /**
     * Cria um aviso (uma única vez por inscrição) para cada inscrição nova
     * que está há mais de {@code dias} dias sem nenhum contato.
     *
     * @return quantos avisos novos foram criados
     */
    @Transactional
    public int verificarInscricoesSemContato(int dias) {
        LocalDateTime limite = LocalDateTime.now().minusDays(dias);
        int criados = 0;
        for (Inscricao inscricao : inscricaoRepository.findByStatusOrderByRecebidaEmDescIdDesc(StatusInscricao.NOVA)) {
            if (inscricao.getRecebidaEm().isAfter(limite)
                    || notificacaoRepository.existsByTipoAndReferenciaId(TipoNotificacao.INSCRICAO_SEM_CONTATO, inscricao.getId())) {
                continue;
            }
            long diasEsperando = Duration.between(inscricao.getRecebidaEm(), LocalDateTime.now()).toDays();
            notificacaoRepository.save(new Notificacao(
                    TipoNotificacao.INSCRICAO_SEM_CONTATO,
                    "Inscrição esperando contato",
                    primeiroNome(inscricao.getNome()) + " se inscreveu "
                            + (diasEsperando == 0 ? "hoje" : diasEsperando == 1 ? "há 1 dia" : "há " + diasEsperando + " dias")
                            + " e ainda não recebeu nenhum contato da equipe.",
                    "/inscricoes/" + inscricao.getId(),
                    inscricao.getId()));
            criados++;
        }
        return criados;
    }

    @Transactional(readOnly = true)
    public List<Notificacao> listarRecentes() {
        return notificacaoRepository.findTop50ByOrderByCriadaEmDescIdDesc();
    }

    @Transactional(readOnly = true)
    public long contarNaoVistas(Long usuarioId) {
        if (usuarioId == null) {
            return 0;
        }
        return usuarioRepository.findById(usuarioId)
                .map(usuario -> notificacaoRepository.countByCriadaEmAfter(vistoAte(usuario)))
                .orElse(0L);
    }

    /** Momento a partir do qual as notificações contam como novas para esta pessoa. */
    @Transactional(readOnly = true)
    public LocalDateTime vistoAte(Long usuarioId) {
        return usuarioRepository.findById(usuarioId).map(NotificacaoService::vistoAte).orElse(LocalDateTime.now());
    }

    @Transactional
    public void marcarComoVistas(Long usuarioId) {
        usuarioRepository.findById(usuarioId).ifPresent(usuario -> {
            usuario.marcarNotificacoesVistas();
            usuarioRepository.save(usuario);
        });
    }

    private static LocalDateTime vistoAte(Usuario usuario) {
        if (usuario.getNotificacoesVistasEm() != null) {
            return usuario.getNotificacoesVistasEm();
        }
        // Conta nova: começa vendo como novos os avisos dos últimos 7 dias.
        LocalDateTime criacao = usuario.getCriadoEm() != null ? usuario.getCriadoEm() : LocalDateTime.now();
        return criacao.minusDays(7);
    }

    private static String primeiroNome(String nome) {
        if (nome == null || nome.isBlank()) {
            return "Uma mulher";
        }
        return nome.trim().split("\\s+")[0];
    }
}
