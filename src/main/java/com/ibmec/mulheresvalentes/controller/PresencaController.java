package com.ibmec.mulheresvalentes.controller;

import com.ibmec.mulheresvalentes.dto.RegistroPresencaForm;
import com.ibmec.mulheresvalentes.dto.CorrecaoPresencaForm;
import com.ibmec.mulheresvalentes.dto.FrequenciaParticipanteDto;
import com.ibmec.mulheresvalentes.dto.ResumoRegistroPresencaDto;
import com.ibmec.mulheresvalentes.dto.OperacaoRevisaoDTO;
import com.ibmec.mulheresvalentes.dto.RevisaoPreparadaDTO;
import com.ibmec.mulheresvalentes.model.Encontro;
import com.ibmec.mulheresvalentes.model.Presenca;
import com.ibmec.mulheresvalentes.model.StatusPresenca;
import com.ibmec.mulheresvalentes.model.TipoOperacaoRevisao;
import com.ibmec.mulheresvalentes.service.AtividadeService;
import com.ibmec.mulheresvalentes.service.EncontroService;
import com.ibmec.mulheresvalentes.service.ParticipanteService;
import com.ibmec.mulheresvalentes.service.PresencaService;
import com.ibmec.mulheresvalentes.service.HistoricoOperacaoService;
import com.ibmec.mulheresvalentes.service.RevisaoOperacaoService;
import jakarta.validation.Valid;
import jakarta.servlet.http.HttpSession;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping
public class PresencaController {

    private final PresencaService presencaService;
    private final ParticipanteService participanteService;
    private final AtividadeService atividadeService;
    private final EncontroService encontroService;
    private final HistoricoOperacaoService historicoService;
    private final RevisaoOperacaoService revisaoService;

    public PresencaController(
            PresencaService presencaService,
            ParticipanteService participanteService,
            AtividadeService atividadeService,
            EncontroService encontroService,
            HistoricoOperacaoService historicoService,
            RevisaoOperacaoService revisaoService
    ) {
        this.presencaService = presencaService;
        this.participanteService = participanteService;
        this.atividadeService = atividadeService;
        this.encontroService = encontroService;
        this.historicoService = historicoService;
        this.revisaoService = revisaoService;
    }

    @GetMapping("/presencas")
    public String historico(
            @RequestParam(required = false) Long participanteId,
            @RequestParam(required = false) Long atividadeId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicial,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFinal,
            @RequestParam(required = false) StatusPresenca status,
            @RequestParam(required = false) String busca,
            Model model
    ) {
        List<Presenca> presencas = presencaService.listarHistorico(
                participanteId, null, atividadeId, dataInicial, dataFinal, status, busca
        );
        model.addAttribute("presencas", presencas);
        model.addAttribute("resumo", presencaService.resumir(presencas));
        model.addAttribute("participantes", participanteService.listar(null));
        model.addAttribute("atividades", atividadeService.listar(null));
        model.addAttribute("statusPresenca", StatusPresenca.values());
        model.addAttribute("participanteId", participanteId);
        model.addAttribute("atividadeId", atividadeId);
        model.addAttribute("dataInicial", dataInicial);
        model.addAttribute("dataFinal", dataFinal);
        model.addAttribute("statusSelecionado", status);
        model.addAttribute("busca", busca);
        return "presencas/historico";
    }

    @GetMapping("/presencas/registrar")
    public String registrar(
            @RequestParam(required = false) Long atividadeId,
            @RequestParam(required = false) Long encontroId,
            @RequestParam(required = false) String busca,
            Model model
    ) {
        RegistroPresencaForm form = encontroId == null
                ? new RegistroPresencaForm()
                : presencaService.prepararRegistroColetivo(encontroId, busca);
        prepararTelaRegistro(model, form, atividadeId, busca);
        return "presencas/registrar";
    }

    @PostMapping(value = "/presencas/registrar", params = "acao=marcarTodos")
    public String marcarTodosComoPresentes(
            @ModelAttribute("registroPresencaForm") RegistroPresencaForm form,
            @RequestParam(required = false) String busca,
            Model model
    ) {
        form.marcarTodosComoPresentes();
        prepararTelaRegistro(model, form, null, busca);
        return "presencas/registrar";
    }

    @PostMapping("/presencas/registrar")
    public String salvarRegistro(
            @Valid @ModelAttribute("registroPresencaForm") RegistroPresencaForm form,
            BindingResult bindingResult,
            @RequestParam(required = false) String busca,
            Model model,
            HttpSession sessao
    ) {
        if (bindingResult.hasErrors()) {
            prepararTelaRegistro(model, form, null, busca);
            return "presencas/registrar";
        }

        return mostrarRevisao(new OperacaoRevisaoDTO(
                TipoOperacaoRevisao.REGISTRAR_PRESENCAS, form), sessao, model);
    }

    @GetMapping("/frequencia")
    public String frequencia(
            @RequestParam(required = false) Long atividadeId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicial,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFinal,
            @RequestParam(required = false) String busca,
            Model model
    ) {
        List<FrequenciaParticipanteDto> frequencias = presencaService.calcularFrequencias(
                atividadeId, dataInicial, dataFinal, busca);
        model.addAttribute("frequencias", frequencias);
        model.addAttribute("resumoFrequencia", presencaService.resumirFrequencias(frequencias));
        model.addAttribute("atividades", atividadeService.listar(null));
        model.addAttribute("atividadeId", atividadeId);
        model.addAttribute("dataInicial", dataInicial);
        model.addAttribute("dataFinal", dataFinal);
        model.addAttribute("busca", busca);
        return "presencas/frequencia";
    }

    @GetMapping("/presencas/{id}/corrigir")
    public String corrigir(@PathVariable Long id, Model model) {
        Presenca presenca = presencaService.buscar(id);
        model.addAttribute("presenca", presenca);
        model.addAttribute("correcaoPresencaForm", CorrecaoPresencaForm.de(presenca));
        model.addAttribute("statusPresenca", StatusPresenca.values());
        model.addAttribute("historicoEntidade", historicoService.listarPorEntidade("Presenca", id));
        return "presencas/corrigir";
    }

    @PostMapping("/presencas/{id}/corrigir")
    public String salvarCorrecao(
            @PathVariable Long id,
            @Valid @ModelAttribute("correcaoPresencaForm") CorrecaoPresencaForm form,
            BindingResult bindingResult,
            Model model,
            HttpSession sessao) {
        Presenca presenca = presencaService.buscar(id);
        if (bindingResult.hasErrors()) {
            model.addAttribute("presenca", presenca);
            model.addAttribute("statusPresenca", StatusPresenca.values());
            return "presencas/corrigir";
        }
        return mostrarRevisao(new OperacaoRevisaoDTO(
                TipoOperacaoRevisao.CORRIGIR_PRESENCA, id, form), sessao, model);
    }

    private void prepararTelaRegistro(
            Model model,
            RegistroPresencaForm form,
            Long atividadeInformada,
            String busca
    ) {
        Long atividadeId = atividadeInformada;
        Encontro encontro = null;
        if (form.getEncontroId() != null) {
            encontro = encontroService.buscar(form.getEncontroId());
            atividadeId = encontro.getAtividade().getId();
        }
        model.addAttribute("registroPresencaForm", form);
        model.addAttribute("atividades", atividadeService.listarAtivas());
        model.addAttribute(
                "encontros",
                atividadeId == null ? List.of() : encontroService.listarDaAtividade(atividadeId)
        );
        model.addAttribute("atividadeId", atividadeId);
        model.addAttribute("encontroSelecionado", encontro);
        model.addAttribute("statusPresenca", StatusPresenca.values());
        model.addAttribute("resumoRegistro", presencaService.resumirFormulario(form));
        model.addAttribute("busca", busca);
    }

    private String mostrarRevisao(OperacaoRevisaoDTO operacao, HttpSession sessao, Model model) {
        RevisaoPreparadaDTO preparada = revisaoService.preparar(sessao, operacao);
        model.addAttribute("revisao", preparada.revisao());
        model.addAttribute("token", preparada.token());
        model.addAttribute("chave", revisaoService.chave(operacao));
        return "confirmacoes/revisao";
    }
}
