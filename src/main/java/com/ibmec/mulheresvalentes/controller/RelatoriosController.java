package com.ibmec.mulheresvalentes.controller;

import com.ibmec.mulheresvalentes.model.Presenca;
import com.ibmec.mulheresvalentes.model.StatusPresenca;
import com.ibmec.mulheresvalentes.service.AtividadeService;
import com.ibmec.mulheresvalentes.service.ParticipanteService;
import com.ibmec.mulheresvalentes.service.PresencaService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/relatorios")
public class RelatoriosController {

    private final PresencaService presencaService;
    private final ParticipanteService participanteService;
    private final AtividadeService atividadeService;

    public RelatoriosController(
            PresencaService presencaService,
            ParticipanteService participanteService,
            AtividadeService atividadeService) {
        this.presencaService = presencaService;
        this.participanteService = participanteService;
        this.atividadeService = atividadeService;
    }

    @GetMapping
    public String inicio() {
        return "relatorios/index";
    }

    @GetMapping("/presenca")
    public String presenca(
            @RequestParam(required = false) Long participanteId,
            @RequestParam(required = false) Long atividadeId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicial,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFinal,
            @RequestParam(required = false) StatusPresenca status,
            @RequestParam(required = false) String busca,
            Model model) {
        List<Presenca> presencas = presencaService.listarHistorico(
                participanteId, null, atividadeId, dataInicial, dataFinal, status, busca);
        model.addAttribute("presencas", presencas);
        model.addAttribute("resumo", presencaService.resumir(presencas));
        model.addAttribute("participanteSelecionado",
                participanteId == null ? null : participanteService.buscar(participanteId));
        model.addAttribute("atividadeSelecionada",
                atividadeId == null ? null : atividadeService.buscar(atividadeId));
        model.addAttribute("dataInicial", dataInicial);
        model.addAttribute("dataFinal", dataFinal);
        model.addAttribute("statusSelecionado", status);
        model.addAttribute("busca", busca);
        return "relatorios/presenca";
    }

    @GetMapping("/frequencia")
    public String frequencia(
            @RequestParam(required = false) Long atividadeId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicial,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFinal,
            @RequestParam(required = false) String busca,
            Model model) {
        model.addAttribute("frequencias",
                presencaService.calcularFrequencias(atividadeId, dataInicial, dataFinal, busca));
        model.addAttribute("atividadeSelecionada",
                atividadeId == null ? null : atividadeService.buscar(atividadeId));
        model.addAttribute("dataInicial", dataInicial);
        model.addAttribute("dataFinal", dataFinal);
        model.addAttribute("busca", busca);
        return "relatorios/frequencia";
    }
}
