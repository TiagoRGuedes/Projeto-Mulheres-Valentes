package com.ibmec.mulheresvalentes.controller;

import com.ibmec.mulheresvalentes.dto.ParticipanteForm;
import com.ibmec.mulheresvalentes.dto.OperacaoRevisaoDTO;
import com.ibmec.mulheresvalentes.dto.RevisaoPreparadaDTO;
import com.ibmec.mulheresvalentes.model.Participante;
import com.ibmec.mulheresvalentes.model.TipoOperacaoRevisao;
import com.ibmec.mulheresvalentes.service.ParticipanteService;
import com.ibmec.mulheresvalentes.service.HistoricoOperacaoService;
import com.ibmec.mulheresvalentes.service.ConexoesAdministrativasService;
import com.ibmec.mulheresvalentes.service.RevisaoOperacaoService;
import com.ibmec.mulheresvalentes.exception.RegraNegocioException;
import jakarta.validation.Valid;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/participantes")
public class ParticipanteController {

    private final ParticipanteService participanteService;
    private final HistoricoOperacaoService historicoService;
    private final ConexoesAdministrativasService conexoesService;
    private final RevisaoOperacaoService revisaoService;

    public ParticipanteController(
            ParticipanteService participanteService,
            HistoricoOperacaoService historicoService,
            ConexoesAdministrativasService conexoesService,
            RevisaoOperacaoService revisaoService) {
        this.participanteService = participanteService;
        this.historicoService = historicoService;
        this.conexoesService = conexoesService;
        this.revisaoService = revisaoService;
    }

    @GetMapping
    public String listar(
            @RequestParam(required = false) String busca,
            Model model
    ) {
        model.addAttribute("participantes", participanteService.listar(busca));
        model.addAttribute("resumoParticipantes", participanteService.resumir());
        model.addAttribute("busca", busca);
        return "participantes/lista";
    }

    @GetMapping("/novo")
    public String novo(Model model) {
        model.addAttribute("participanteForm", new ParticipanteForm());
        model.addAttribute("modoEdicao", false);
        return "participantes/formulario";
    }

    @PostMapping
    public String cadastrar(
            @Valid @ModelAttribute("participanteForm") ParticipanteForm form,
            BindingResult bindingResult,
            Model model,
            HttpSession sessao
    ) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("modoEdicao", false);
            return "participantes/formulario";
        }
        return mostrarRevisao(new OperacaoRevisaoDTO(
                TipoOperacaoRevisao.CRIAR_PARTICIPANTE, form), sessao, model);
    }

    @GetMapping("/{id}")
    public String visualizar(@PathVariable Long id, Model model) {
        Participante participante = participanteService.buscar(id);
        model.addAttribute("participante", participante);
        model.addAttribute("podeExcluir", participanteService.podeExcluir(id));
        model.addAttribute("conexoes", conexoesService.participante(id));
        model.addAttribute("historicoEntidade", historicoService.listarPorEntidade("Participante", id));
        return "participantes/detalhes";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model) {
        Participante participante = participanteService.buscar(id);
        model.addAttribute("participante", participante);
        model.addAttribute("participanteId", participante.getId());
        model.addAttribute("participanteForm", ParticipanteForm.de(participante));
        model.addAttribute("modoEdicao", true);
        return "participantes/formulario";
    }

    @PostMapping("/{id}")
    public String atualizar(
            @PathVariable Long id,
            @Valid @ModelAttribute("participanteForm") ParticipanteForm form,
            BindingResult bindingResult,
            Model model,
            HttpSession sessao
    ) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("participanteId", id);
            model.addAttribute("modoEdicao", true);
            return "participantes/formulario";
        }
        return mostrarRevisao(new OperacaoRevisaoDTO(
                TipoOperacaoRevisao.EDITAR_PARTICIPANTE, id, form), sessao, model);
    }

    @PostMapping("/{id}/ativar")
    public String ativar(@PathVariable Long id, HttpSession sessao, Model model) {
        return mostrarRevisao(new OperacaoRevisaoDTO(
                TipoOperacaoRevisao.ATIVAR_PARTICIPANTE, id), sessao, model);
    }

    @PostMapping("/{id}/desativar")
    public String desativar(@PathVariable Long id, HttpSession sessao, Model model) {
        return mostrarRevisao(new OperacaoRevisaoDTO(
                TipoOperacaoRevisao.DESATIVAR_PARTICIPANTE, id), sessao, model);
    }

    @PostMapping("/{id}/excluir")
    public String excluir(@PathVariable Long id, HttpSession sessao, Model model) {
        return mostrarRevisao(new OperacaoRevisaoDTO(
                TipoOperacaoRevisao.EXCLUIR_PARTICIPANTE, id), sessao, model);
    }

    private String mostrarRevisao(OperacaoRevisaoDTO operacao, HttpSession sessao, Model model) {
        RevisaoPreparadaDTO preparada = revisaoService.preparar(sessao, operacao);
        model.addAttribute("revisao", preparada.revisao());
        model.addAttribute("token", preparada.token());
        model.addAttribute("chave", revisaoService.chave(operacao));
        return "confirmacoes/revisao";
    }
}
