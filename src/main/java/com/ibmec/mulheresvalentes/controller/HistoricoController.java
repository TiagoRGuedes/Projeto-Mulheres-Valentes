package com.ibmec.mulheresvalentes.controller;

import com.ibmec.mulheresvalentes.exception.RegraNegocioException;
import com.ibmec.mulheresvalentes.model.AcaoHistorico;
import com.ibmec.mulheresvalentes.model.HistoricoOperacao;
import com.ibmec.mulheresvalentes.model.ModuloHistorico;
import com.ibmec.mulheresvalentes.service.HistoricoOperacaoService;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;

@Controller
public class HistoricoController {

    private final HistoricoOperacaoService historicoService;

    public HistoricoController(HistoricoOperacaoService historicoService) {
        this.historicoService = historicoService;
    }

    @GetMapping("/historico")
    public String historico(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicial,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFinal,
            @RequestParam(required = false) ModuloHistorico modulo,
            @RequestParam(required = false) AcaoHistorico acao,
            @RequestParam(required = false) String busca,
            @RequestParam(defaultValue = "0") int pagina,
            Model model) {
        Page<HistoricoOperacao> resultado;
        try {
            resultado = historicoService.pesquisar(
                    dataInicial, dataFinal, modulo, acao, busca, pagina);
        } catch (RegraNegocioException ex) {
            model.addAttribute("erroFiltro", ex.getMessage());
            resultado = historicoService.pesquisar(null, null, modulo, acao, busca, 0);
        }
        model.addAttribute("pagina", resultado);
        model.addAttribute("grupos", historicoService.agruparPorDia(resultado.getContent()));
        model.addAttribute("modulos", ModuloHistorico.values());
        model.addAttribute("acoes", AcaoHistorico.values());
        model.addAttribute("dataInicial", dataInicial);
        model.addAttribute("dataFinal", dataFinal);
        model.addAttribute("moduloSelecionado", modulo);
        model.addAttribute("acaoSelecionada", acao);
        model.addAttribute("busca", busca);
        return "historico/index";
    }
}
