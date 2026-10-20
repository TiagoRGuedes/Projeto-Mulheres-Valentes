package com.ibmec.mulheresvalentes.config;

import com.ibmec.mulheresvalentes.controller.AcessibilidadeController;
import com.ibmec.mulheresvalentes.dto.PreferenciasAcessibilidade;
import com.ibmec.mulheresvalentes.security.UsuarioAutenticado;
import com.ibmec.mulheresvalentes.security.UsuarioLogado;
import com.ibmec.mulheresvalentes.service.InscricaoService;
import com.ibmec.mulheresvalentes.service.NotificacaoService;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class ModeloGlobalAdvice {

    private final InscricaoService inscricaoService;
    private final NotificacaoService notificacaoService;

    public ModeloGlobalAdvice(InscricaoService inscricaoService, NotificacaoService notificacaoService) {
        this.inscricaoService = inscricaoService;
        this.notificacaoService = notificacaoService;
    }

    @ModelAttribute("preferencias")
    public PreferenciasAcessibilidade preferencias(HttpSession session) {
        Object valor = session.getAttribute(AcessibilidadeController.CHAVE_SESSAO);
        if (valor instanceof PreferenciasAcessibilidade preferencias) {
            return preferencias;
        }
        PreferenciasAcessibilidade padrao = PreferenciasAcessibilidade.padrao();
        session.setAttribute(AcessibilidadeController.CHAVE_SESSAO, padrao);
        return padrao;
    }

    @ModelAttribute("usuarioLogado")
    public UsuarioAutenticado usuarioLogado() {
        return UsuarioLogado.atual().orElse(null);
    }

    @ModelAttribute("notificacoesNaoLidas")
    public long notificacoesNaoLidas() {
        return UsuarioLogado.atual().map(u -> notificacaoService.contarNaoVistas(u.getId())).orElse(0L);
    }

    @ModelAttribute("inscricoesNovas")
    public long inscricoesNovas() {
        return inscricaoService.contarNovas();
    }
}
