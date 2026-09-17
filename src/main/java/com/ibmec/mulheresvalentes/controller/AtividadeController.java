package com.ibmec.mulheresvalentes.controller;

import com.ibmec.mulheresvalentes.dto.AtividadeForm;
import com.ibmec.mulheresvalentes.dto.OperacaoRevisaoDTO;
import com.ibmec.mulheresvalentes.dto.RevisaoPreparadaDTO;
import com.ibmec.mulheresvalentes.model.Atividade;
import com.ibmec.mulheresvalentes.model.TipoOperacaoRevisao;
import com.ibmec.mulheresvalentes.service.AtividadeService;
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
@RequestMapping("/atividades")
public class AtividadeController {

    private final AtividadeService atividadeService;
    private final HistoricoOperacaoService historicoService;
    private final ConexoesAdministrativasService conexoesService;
    private final RevisaoOperacaoService revisaoService;

    public AtividadeController(
            AtividadeService atividadeService,
            HistoricoOperacaoService historicoService,
            ConexoesAdministrativasService conexoesService,
            RevisaoOperacaoService revisaoService) {
        this.atividadeService = atividadeService;
        this.historicoService = historicoService;
        this.conexoesService = conexoesService;
        this.revisaoService = revisaoService;
    }

    @GetMapping
    public String listar(@RequestParam(required = false) String busca, Model model) {
        model.addAttribute("atividades", atividadeService.listar(busca));
        model.addAttribute("resumoAtividades", atividadeService.resumir());
        model.addAttribute("busca", busca);
        return "atividades/lista";
    }

    @GetMapping("/nova")
    public String nova(Model model) {
        model.addAttribute("atividadeForm", new AtividadeForm());
        model.addAttribute("modoEdicao", false);
        return "atividades/formulario";
    }

    @PostMapping
    public String cadastrar(
            @Valid @ModelAttribute("atividadeForm") AtividadeForm form,
            BindingResult bindingResult,
            Model model,
            HttpSession sessao
    ) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("modoEdicao", false);
            return "atividades/formulario";
        }
        return mostrarRevisao(new OperacaoRevisaoDTO(
                TipoOperacaoRevisao.CRIAR_ATIVIDADE, form), sessao, model);
    }

    @GetMapping("/{id}")
    public String visualizar(@PathVariable Long id, Model model) {
        Atividade atividade = atividadeService.buscar(id);
        model.addAttribute("atividade", atividade);
        model.addAttribute("podeExcluir", atividadeService.podeExcluir(id));
        model.addAttribute("conexoes", conexoesService.atividade(id));
        model.addAttribute("historicoEntidade", historicoService.listarPorEntidade("Atividade", id));
        return "atividades/detalhes";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model) {
        Atividade atividade = atividadeService.buscar(id);
        model.addAttribute("atividade", atividade);
        model.addAttribute("atividadeId", atividade.getId());
        model.addAttribute("atividadeForm", AtividadeForm.de(atividade));
        model.addAttribute("modoEdicao", true);
        return "atividades/formulario";
    }

    @PostMapping("/{id}")
    public String atualizar(
            @PathVariable Long id,
            @Valid @ModelAttribute("atividadeForm") AtividadeForm form,
            BindingResult bindingResult,
            Model model,
            HttpSession sessao
    ) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("atividadeId", id);
            model.addAttribute("modoEdicao", true);
            return "atividades/formulario";
        }
        return mostrarRevisao(new OperacaoRevisaoDTO(
                TipoOperacaoRevisao.EDITAR_ATIVIDADE, id, form), sessao, model);
    }

    @PostMapping("/{id}/ativar")
    public String ativar(@PathVariable Long id, HttpSession sessao, Model model) {
        return mostrarRevisao(new OperacaoRevisaoDTO(
                TipoOperacaoRevisao.ATIVAR_ATIVIDADE, id), sessao, model);
    }

    @PostMapping("/{id}/desativar")
    public String desativar(@PathVariable Long id, HttpSession sessao, Model model) {
        return mostrarRevisao(new OperacaoRevisaoDTO(
                TipoOperacaoRevisao.DESATIVAR_ATIVIDADE, id), sessao, model);
    }

    @PostMapping("/{id}/excluir")
    public String excluir(@PathVariable Long id, HttpSession sessao, Model model) {
        return mostrarRevisao(new OperacaoRevisaoDTO(
                TipoOperacaoRevisao.EXCLUIR_ATIVIDADE, id), sessao, model);
    }

    private String mostrarRevisao(OperacaoRevisaoDTO operacao, HttpSession sessao, Model model) {
        RevisaoPreparadaDTO preparada = revisaoService.preparar(sessao, operacao);
        model.addAttribute("revisao", preparada.revisao());
        model.addAttribute("token", preparada.token());
        model.addAttribute("chave", revisaoService.chave(operacao));
        return "confirmacoes/revisao";
    }
}
