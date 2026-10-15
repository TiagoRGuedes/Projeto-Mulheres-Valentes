package com.ibmec.mulheresvalentes.controller;

import com.ibmec.mulheresvalentes.dto.OperacaoRevisaoDTO;
import com.ibmec.mulheresvalentes.dto.RevisaoPreparadaDTO;
import com.ibmec.mulheresvalentes.model.Inscricao;
import com.ibmec.mulheresvalentes.model.StatusInscricao;
import com.ibmec.mulheresvalentes.model.TipoOperacaoRevisao;
import com.ibmec.mulheresvalentes.service.HistoricoOperacaoService;
import com.ibmec.mulheresvalentes.service.InscricaoService;
import com.ibmec.mulheresvalentes.service.RevisaoOperacaoService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/inscricoes")
public class InscricaoController {

    private final InscricaoService inscricaoService;
    private final HistoricoOperacaoService historicoService;
    private final RevisaoOperacaoService revisaoService;

    public InscricaoController(
            InscricaoService inscricaoService,
            HistoricoOperacaoService historicoService,
            RevisaoOperacaoService revisaoService) {
        this.inscricaoService = inscricaoService;
        this.historicoService = historicoService;
        this.revisaoService = revisaoService;
    }

    @GetMapping
    public String listar(@RequestParam(required = false) StatusInscricao status, Model model) {
        model.addAttribute("inscricoes", inscricaoService.listar(status));
        model.addAttribute("resumoInscricoes", inscricaoService.resumir());
        model.addAttribute("statusSelecionado", status);
        model.addAttribute("statusInscricao", StatusInscricao.values());
        return "inscricoes/lista";
    }

    @GetMapping("/{id}")
    public String detalhar(@PathVariable Long id, Model model) {
        Inscricao inscricao = inscricaoService.buscar(id);
        model.addAttribute("inscricao", inscricao);
        model.addAttribute("participanteExistente",
                inscricao.getStatus().isAberta()
                        ? inscricaoService.participanteComMesmoContato(inscricao).orElse(null)
                        : null);
        model.addAttribute("historicoEntidade", historicoService.listarPorEntidade("Inscricao", id));
        return "inscricoes/detalhes";
    }

    @PostMapping("/{id}/em-contato")
    public String marcarEmContato(@PathVariable Long id, HttpSession sessao, Model model) {
        return mostrarRevisao(new OperacaoRevisaoDTO(
                TipoOperacaoRevisao.MARCAR_INSCRICAO_EM_CONTATO, id), sessao, model);
    }

    @PostMapping("/{id}/aceitar")
    public String aceitar(@PathVariable Long id, HttpSession sessao, Model model) {
        return mostrarRevisao(new OperacaoRevisaoDTO(
                TipoOperacaoRevisao.ACEITAR_INSCRICAO, id), sessao, model);
    }

    @PostMapping("/{id}/arquivar")
    public String arquivar(@PathVariable Long id, HttpSession sessao, Model model) {
        return mostrarRevisao(new OperacaoRevisaoDTO(
                TipoOperacaoRevisao.ARQUIVAR_INSCRICAO, id), sessao, model);
    }

    private String mostrarRevisao(OperacaoRevisaoDTO operacao, HttpSession sessao, Model model) {
        RevisaoPreparadaDTO preparada = revisaoService.preparar(sessao, operacao);
        model.addAttribute("revisao", preparada.revisao());
        model.addAttribute("token", preparada.token());
        model.addAttribute("chave", revisaoService.chave(operacao));
        return "confirmacoes/revisao";
    }
}
