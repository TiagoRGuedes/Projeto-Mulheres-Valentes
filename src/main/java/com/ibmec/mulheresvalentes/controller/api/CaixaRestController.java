package com.ibmec.mulheresvalentes.controller.api;

import com.ibmec.mulheresvalentes.dto.ExtratoCaixaDTO;
import com.ibmec.mulheresvalentes.dto.LancamentoCaixaFiltro;
import com.ibmec.mulheresvalentes.dto.LancamentoCaixaRequest;
import com.ibmec.mulheresvalentes.dto.LancamentoCaixaResponse;
import com.ibmec.mulheresvalentes.model.LancamentoCaixa;
import com.ibmec.mulheresvalentes.model.NaturezaLancamento;
import com.ibmec.mulheresvalentes.model.TipoLancamento;
import com.ibmec.mulheresvalentes.service.LancamentoCaixaService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.math.BigDecimal;
import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/caixa")
public class CaixaRestController {

    private final LancamentoCaixaService lancamentoService;

    public CaixaRestController(LancamentoCaixaService lancamentoService) {
        this.lancamentoService = lancamentoService;
    }

    @GetMapping
    public List<LancamentoCaixaResponse> listar(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicial,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFinal,
            @RequestParam(required = false) TipoLancamento tipo,
            @RequestParam(required = false) NaturezaLancamento natureza,
            @RequestParam(required = false) Long categoriaId,
            @RequestParam(required = false) String descricao) {
        return lancamentoService.pesquisarRespostas(
                filtro(dataInicial, dataFinal, tipo, natureza, categoriaId, descricao));
    }

    @GetMapping("/saldo")
    public Map<String, BigDecimal> saldo() {
        return Map.of("saldoAtual", lancamentoService.calcularSaldoAtual());
    }

    @GetMapping("/extrato")
    public ExtratoCaixaDTO extrato(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicial,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFinal,
            @RequestParam(required = false) TipoLancamento tipo,
            @RequestParam(required = false) NaturezaLancamento natureza,
            @RequestParam(required = false) Long categoriaId,
            @RequestParam(required = false) String descricao) {
        LancamentoCaixaFiltro filtro =
                filtro(dataInicial, dataFinal, tipo, natureza, categoriaId, descricao);
        return new ExtratoCaixaDTO(
                lancamentoService.calcularResumo(filtro),
                lancamentoService.pesquisarRespostas(filtro));
    }

    @PostMapping
    public ResponseEntity<LancamentoCaixaResponse> criar(
            @Valid @RequestBody LancamentoCaixaRequest request) {
        LancamentoCaixa criado = lancamentoService.criar(request);
        URI local = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(criado.getId())
                .toUri();
        return ResponseEntity.created(local).body(LancamentoCaixaResponse.from(criado));
    }

    @GetMapping("/{id}")
    public LancamentoCaixaResponse buscar(@PathVariable Long id) {
        return lancamentoService.buscarRespostaPorId(id);
    }

    @PutMapping("/{id}")
    public LancamentoCaixaResponse atualizar(
            @PathVariable Long id,
            @Valid @RequestBody LancamentoCaixaRequest request) {
        return LancamentoCaixaResponse.from(lancamentoService.atualizar(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        lancamentoService.excluir(id);
        return ResponseEntity.noContent().build();
    }

    static LancamentoCaixaFiltro filtro(
            LocalDate dataInicial,
            LocalDate dataFinal,
            TipoLancamento tipo,
            NaturezaLancamento natureza,
            Long categoriaId,
            String descricao) {
        LancamentoCaixaFiltro filtro = new LancamentoCaixaFiltro();
        filtro.setDataInicial(dataInicial);
        filtro.setDataFinal(dataFinal);
        filtro.setTipo(tipo);
        filtro.setNatureza(natureza);
        filtro.setCategoriaId(categoriaId);
        filtro.setDescricao(descricao);
        return filtro;
    }
}
