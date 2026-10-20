package com.ibmec.mulheresvalentes.controller;

import com.ibmec.mulheresvalentes.dto.ContatoInscricaoForm;
import com.ibmec.mulheresvalentes.dto.OperacaoRevisaoDTO;
import com.ibmec.mulheresvalentes.exception.RegraNegocioException;
import com.ibmec.mulheresvalentes.model.CanalContato;
import com.ibmec.mulheresvalentes.model.ResultadoContato;
import com.ibmec.mulheresvalentes.service.ContatoInscricaoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.time.LocalDateTime;
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
    private final ContatoInscricaoService contatoService;
    private final int diasSemContato;

    public InscricaoController(
            InscricaoService inscricaoService,
            HistoricoOperacaoService historicoService,
            RevisaoOperacaoService revisaoService,
            ContatoInscricaoService contatoService,
            @Value("${app.inscricoes.dias-sem-contato:3}") int diasSemContato) {
        this.inscricaoService = inscricaoService;
        this.historicoService = historicoService;
        this.revisaoService = revisaoService;
        this.contatoService = contatoService;
        this.diasSemContato = diasSemContato;
    }

    @GetMapping
    public String listar(@RequestParam(required = false) StatusInscricao status, Model model) {
        model.addAttribute("inscricoes", inscricaoService.listar(status));
        model.addAttribute("resumoInscricoes", inscricaoService.resumir());
        model.addAttribute("statusSelecionado", status);
        model.addAttribute("statusInscricao", StatusInscricao.values());
        model.addAttribute("resumoContatos", contatoService.resumirTodas());
        model.addAttribute("limiteSemContato", LocalDateTime.now().minusDays(diasSemContato));
        model.addAttribute("diasSemContato", diasSemContato);
        return "inscricoes/lista";
    }

    @GetMapping("/{id}")
    public String detalhar(@PathVariable Long id, Model model) {
        Inscricao inscricao = inscricaoService.buscar(id);
        if (!model.containsAttribute("contatoForm")) {
            model.addAttribute("contatoForm", contatoService.formularioPara(inscricao));
        }
        return mostrarDetalhes(inscricao, model);
    }

    @PostMapping("/{id}/contatos")
    public String registrarContato(
            @PathVariable Long id,
            @Valid @ModelAttribute("contatoForm") ContatoInscricaoForm form,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {
        Inscricao inscricao = inscricaoService.buscar(id);
        if (bindingResult.hasErrors()) {
            model.addAttribute("contatoComErro", true);
            return mostrarDetalhes(inscricao, model);
        }
        boolean eraNova = inscricao.getStatus() == StatusInscricao.NOVA;
        try {
            contatoService.registrar(id, form);
        } catch (RegraNegocioException ex) {
            bindingResult.reject("contato.invalido", ex.getMessage());
            model.addAttribute("contatoComErro", true);
            return mostrarDetalhes(inscricao, model);
        }
        redirectAttributes.addFlashAttribute("sucesso", eraNova
                ? "Contato registrado. A inscrição passou para \"Em contato\"."
                : "Contato registrado.");
        return "redirect:/inscricoes/" + id + "#contatos";
    }

    private String mostrarDetalhes(Inscricao inscricao, Model model) {
        Long id = inscricao.getId();
        model.addAttribute("inscricao", inscricao);
        model.addAttribute("participanteExistente",
                inscricao.getStatus().isAberta()
                        ? inscricaoService.participanteComMesmoContato(inscricao).orElse(null)
                        : null);
        model.addAttribute("historicoEntidade", historicoService.listarPorEntidade("Inscricao", id));
        model.addAttribute("contatos", contatoService.listar(id));
        model.addAttribute("canaisContato", CanalContato.values());
        model.addAttribute("resultadosContato", ResultadoContato.values());
        model.addAttribute("agora", LocalDateTime.now());
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
