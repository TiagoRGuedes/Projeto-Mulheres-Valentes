package com.ibmec.mulheresvalentes.controller;

import com.ibmec.mulheresvalentes.dto.EncontroForm;
import com.ibmec.mulheresvalentes.dto.OperacaoRevisaoDTO;
import com.ibmec.mulheresvalentes.dto.RevisaoPreparadaDTO;
import com.ibmec.mulheresvalentes.model.Encontro;
import com.ibmec.mulheresvalentes.model.TipoOperacaoRevisao;
import com.ibmec.mulheresvalentes.service.AtividadeService;
import com.ibmec.mulheresvalentes.service.EncontroService;
import com.ibmec.mulheresvalentes.service.HistoricoOperacaoService;
import com.ibmec.mulheresvalentes.service.ConexoesAdministrativasService;
import com.ibmec.mulheresvalentes.service.RevisaoOperacaoService;
import com.ibmec.mulheresvalentes.exception.RegraNegocioException;
import jakarta.validation.Valid;
import jakarta.servlet.http.HttpSession;
import org.springframework.format.annotation.DateTimeFormat;
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

import java.time.LocalDate;

@Controller
@RequestMapping("/encontros")
public class EncontroController {

    private final EncontroService encontroService;
    private final AtividadeService atividadeService;
    private final HistoricoOperacaoService historicoService;
    private final ConexoesAdministrativasService conexoesService;
    private final RevisaoOperacaoService revisaoService;

    public EncontroController(
            EncontroService encontroService,
            AtividadeService atividadeService,
            HistoricoOperacaoService historicoService,
            ConexoesAdministrativasService conexoesService,
            RevisaoOperacaoService revisaoService) {
        this.encontroService = encontroService;
        this.atividadeService = atividadeService;
        this.historicoService = historicoService;
        this.conexoesService = conexoesService;
        this.revisaoService = revisaoService;
    }

    @GetMapping
    public String listar(
            @RequestParam(required = false) Long atividadeId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicial,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFinal,
            Model model
    ) {
        model.addAttribute("encontros", encontroService.listar(atividadeId, dataInicial, dataFinal));
        model.addAttribute("resumoEncontros", encontroService.resumir());
        model.addAttribute("atividades", atividadeService.listar(null));
        model.addAttribute("atividadeId", atividadeId);
        model.addAttribute("dataInicial", dataInicial);
        model.addAttribute("dataFinal", dataFinal);
        return "atividades/encontros";
    }

    @GetMapping("/novo")
    public String novo(
            @RequestParam(required = false) Long atividadeId,
            Model model
    ) {
        EncontroForm form = new EncontroForm();
        form.setAtividadeId(atividadeId);
        form.setData(LocalDate.now());
        prepararFormulario(model, form, false, null);
        return "atividades/encontro-formulario";
    }

    @PostMapping
    public String cadastrar(
            @Valid @ModelAttribute("encontroForm") EncontroForm form,
            BindingResult bindingResult,
            Model model,
            HttpSession sessao
    ) {
        if (bindingResult.hasErrors()) {
            prepararFormulario(model, form, false, null);
            return "atividades/encontro-formulario";
        }
        return mostrarRevisao(new OperacaoRevisaoDTO(
                TipoOperacaoRevisao.CRIAR_ENCONTRO, form), sessao, model);
    }

    @GetMapping("/{id}")
    public String visualizar(@PathVariable Long id, Model model) {
        Encontro encontro = encontroService.buscar(id);
        model.addAttribute("encontro", encontro);
        model.addAttribute("podeExcluir", encontroService.podeExcluir(id));
        model.addAttribute("conexoes", conexoesService.encontro(id));
        model.addAttribute("historicoEntidade", historicoService.listarPorEntidade("Encontro", id));
        return "atividades/encontro-detalhes";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model) {
        Encontro encontro = encontroService.buscar(id);
        prepararFormulario(model, EncontroForm.de(encontro), true, id);
        return "atividades/encontro-formulario";
    }

    @PostMapping("/{id}")
    public String atualizar(
            @PathVariable Long id,
            @Valid @ModelAttribute("encontroForm") EncontroForm form,
            BindingResult bindingResult,
            Model model,
            HttpSession sessao
    ) {
        if (bindingResult.hasErrors()) {
            prepararFormulario(model, form, true, id);
            return "atividades/encontro-formulario";
        }
        return mostrarRevisao(new OperacaoRevisaoDTO(
                TipoOperacaoRevisao.EDITAR_ENCONTRO, id, form), sessao, model);
    }

    @PostMapping("/{id}/excluir")
    public String excluir(@PathVariable Long id, HttpSession sessao, Model model) {
        return mostrarRevisao(new OperacaoRevisaoDTO(
                TipoOperacaoRevisao.EXCLUIR_ENCONTRO, id), sessao, model);
    }

    private void prepararFormulario(Model model, EncontroForm form, boolean modoEdicao, Long encontroId) {
        model.addAttribute("encontroForm", form);
        model.addAttribute("atividades", atividadeService.listarAtivas());
        model.addAttribute("modoEdicao", modoEdicao);
        model.addAttribute("encontroId", encontroId);
    }

    private String mostrarRevisao(OperacaoRevisaoDTO operacao, HttpSession sessao, Model model) {
        RevisaoPreparadaDTO preparada = revisaoService.preparar(sessao, operacao);
        model.addAttribute("revisao", preparada.revisao());
        model.addAttribute("token", preparada.token());
        model.addAttribute("chave", revisaoService.chave(operacao));
        return "confirmacoes/revisao";
    }
}
