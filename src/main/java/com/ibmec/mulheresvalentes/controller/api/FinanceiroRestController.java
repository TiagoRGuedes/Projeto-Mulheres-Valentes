package com.ibmec.mulheresvalentes.controller.api;

import com.ibmec.mulheresvalentes.dto.ResumoFinanceiroDTO;
import com.ibmec.mulheresvalentes.model.NaturezaLancamento;
import com.ibmec.mulheresvalentes.model.TipoLancamento;
import com.ibmec.mulheresvalentes.service.LancamentoCaixaService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/financeiro")
public class FinanceiroRestController {

    private final LancamentoCaixaService lancamentoService;

    public FinanceiroRestController(LancamentoCaixaService lancamentoService) {
        this.lancamentoService = lancamentoService;
    }

    @GetMapping("/resumo")
    public ResumoFinanceiroDTO resumo(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicial,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFinal,
            @RequestParam(required = false) TipoLancamento tipo,
            @RequestParam(required = false) NaturezaLancamento natureza,
            @RequestParam(required = false) Long categoriaId,
            @RequestParam(required = false) String descricao) {
        return lancamentoService.calcularResumo(CaixaRestController.filtro(
                dataInicial, dataFinal, tipo, natureza, categoriaId, descricao));
    }
}
