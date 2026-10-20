package com.ibmec.mulheresvalentes.controller;

import com.ibmec.mulheresvalentes.model.Notificacao;
import com.ibmec.mulheresvalentes.model.TipoNotificacao;
import com.ibmec.mulheresvalentes.security.UsuarioAutenticado;
import com.ibmec.mulheresvalentes.service.ContatoInscricaoService;
import com.ibmec.mulheresvalentes.service.NotificacaoService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
public class NotificacaoController {

    private final NotificacaoService notificacaoService;
    private final ContatoInscricaoService contatoService;
    private final int diasSemContato;

    public NotificacaoController(
            NotificacaoService notificacaoService,
            ContatoInscricaoService contatoService,
            @Value("${app.inscricoes.dias-sem-contato:3}") int diasSemContato) {
        this.notificacaoService = notificacaoService;
        this.contatoService = contatoService;
        this.diasSemContato = diasSemContato;
    }

    /** Mostra os avisos agrupados por dia e, ao abrir a página, marca todos como vistos para esta pessoa. */
    @GetMapping("/notificacoes")
    public String listar(
            @AuthenticationPrincipal UsuarioAutenticado logado,
            @RequestParam(required = false) TipoNotificacao tipo,
            Model model) {
        List<Notificacao> todas = notificacaoService.listarRecentes();
        LocalDateTime vistoAte = notificacaoService.vistoAte(logado.getId());

        List<Notificacao> filtradas = tipo == null
                ? todas
                : todas.stream().filter(n -> n.getTipo() == tipo).toList();
        Map<LocalDate, List<Notificacao>> porDia = new LinkedHashMap<>();
        for (Notificacao n : filtradas) {
            porDia.computeIfAbsent(n.getCriadaEm().toLocalDate(), dia -> new java.util.ArrayList<>()).add(n);
        }

        model.addAttribute("gruposPorDia", porDia);
        model.addAttribute("vistoAte", vistoAte);
        model.addAttribute("tipoSelecionado", tipo);
        model.addAttribute("tipos", TipoNotificacao.values());
        model.addAttribute("totalNovas", todas.stream().filter(n -> n.getCriadaEm().isAfter(vistoAte)).count());
        model.addAttribute("totalPorTipo", contar(todas));
        model.addAttribute("totalGeral", todas.size());
        model.addAttribute("inscricoesAtrasadas", contatoService.esperandoPrimeiroContato(diasSemContato).size());
        model.addAttribute("diasSemContato", diasSemContato);

        notificacaoService.marcarComoVistas(logado.getId());
        model.addAttribute("notificacoesNaoLidas", 0L);
        return "notificacoes/lista";
    }

    private static Map<TipoNotificacao, Long> contar(List<Notificacao> notificacoes) {
        Map<TipoNotificacao, Long> total = new LinkedHashMap<>();
        for (TipoNotificacao tipo : TipoNotificacao.values()) {
            total.put(tipo, notificacoes.stream().filter(n -> n.getTipo() == tipo).count());
        }
        return total;
    }
}
