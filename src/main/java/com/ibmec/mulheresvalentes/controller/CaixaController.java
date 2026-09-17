package com.ibmec.mulheresvalentes.controller;

import com.ibmec.mulheresvalentes.dto.CategoriaFinanceiraForm;
import com.ibmec.mulheresvalentes.dto.LancamentoCaixaFiltro;
import com.ibmec.mulheresvalentes.dto.LancamentoCaixaForm;
import com.ibmec.mulheresvalentes.dto.ResumoFinanceiroDTO;
import com.ibmec.mulheresvalentes.dto.OperacaoRevisaoDTO;
import com.ibmec.mulheresvalentes.dto.RevisaoPreparadaDTO;
import com.ibmec.mulheresvalentes.exception.RecursoNaoEncontradoException;
import com.ibmec.mulheresvalentes.exception.RegraNegocioException;
import com.ibmec.mulheresvalentes.model.CategoriaFinanceira;
import com.ibmec.mulheresvalentes.model.LancamentoCaixa;
import com.ibmec.mulheresvalentes.model.TipoLancamento;
import com.ibmec.mulheresvalentes.model.TipoOperacaoRevisao;
import com.ibmec.mulheresvalentes.service.CategoriaFinanceiraService;
import com.ibmec.mulheresvalentes.service.LancamentoCaixaService;
import com.ibmec.mulheresvalentes.service.HistoricoOperacaoService;
import com.ibmec.mulheresvalentes.service.RevisaoOperacaoService;
import jakarta.validation.Valid;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/caixa")
public class CaixaController {

    private final LancamentoCaixaService lancamentoService;
    private final CategoriaFinanceiraService categoriaService;
    private final HistoricoOperacaoService historicoService;
    private final RevisaoOperacaoService revisaoService;

    public CaixaController(
            LancamentoCaixaService lancamentoService,
            CategoriaFinanceiraService categoriaService,
            HistoricoOperacaoService historicoService,
            RevisaoOperacaoService revisaoService) {
        this.lancamentoService = lancamentoService;
        this.categoriaService = categoriaService;
        this.historicoService = historicoService;
        this.revisaoService = revisaoService;
    }

    @ModelAttribute("tiposLancamento")
    public TipoLancamento[] tiposLancamento() {
        return TipoLancamento.values();
    }

    @GetMapping
    public String painel(
            @ModelAttribute("filtro") LancamentoCaixaFiltro filtro,
            Model model) {
        model.addAttribute("paginaAtual", "caixa");
        model.addAttribute("ultimosLancamentos", lancamentoService.listarRecentes());
        model.addAttribute("categorias", categoriaService.listarAtivas());
        adicionarResumoComTratamento(filtro, model);
        return "caixa/index";
    }

    @GetMapping("/entrada")
    public String formularioEntrada(Model model) {
        if (!model.containsAttribute("lancamentoForm")) {
            LancamentoCaixaForm form = new LancamentoCaixaForm();
            form.setTipo(TipoLancamento.ENTRADA);
            model.addAttribute("lancamentoForm", form);
        }
        prepararFormulario(model, TipoLancamento.ENTRADA, false, null);
        return "caixa/entrada";
    }

    @PostMapping("/entrada")
    public String registrarEntrada(
            @Valid @ModelAttribute("lancamentoForm") LancamentoCaixaForm form,
            BindingResult bindingResult,
            Model model,
            HttpSession sessao) {
        form.setTipo(TipoLancamento.ENTRADA);
        if (bindingResult.hasErrors()) {
            prepararFormulario(model, TipoLancamento.ENTRADA, false, null);
            return "caixa/entrada";
        }
        try {
            return mostrarRevisao(new OperacaoRevisaoDTO(
                    TipoOperacaoRevisao.REGISTRAR_ENTRADA, form), sessao, model);
        } catch (RegraNegocioException | RecursoNaoEncontradoException ex) {
            bindingResult.reject("lancamento.invalido", ex.getMessage());
            prepararFormulario(model, TipoLancamento.ENTRADA, false, null);
            return "caixa/entrada";
        }
    }

    @GetMapping("/saida")
    public String formularioSaida(Model model) {
        if (!model.containsAttribute("lancamentoForm")) {
            LancamentoCaixaForm form = new LancamentoCaixaForm();
            form.setTipo(TipoLancamento.SAIDA);
            model.addAttribute("lancamentoForm", form);
        }
        prepararFormulario(model, TipoLancamento.SAIDA, false, null);
        return "caixa/saida";
    }

    @PostMapping("/saida")
    public String registrarSaida(
            @Valid @ModelAttribute("lancamentoForm") LancamentoCaixaForm form,
            BindingResult bindingResult,
            Model model,
            HttpSession sessao) {
        form.setTipo(TipoLancamento.SAIDA);
        if (bindingResult.hasErrors()) {
            prepararFormulario(model, TipoLancamento.SAIDA, false, null);
            return "caixa/saida";
        }
        try {
            return mostrarRevisao(new OperacaoRevisaoDTO(
                    TipoOperacaoRevisao.REGISTRAR_SAIDA, form), sessao, model);
        } catch (RegraNegocioException | RecursoNaoEncontradoException ex) {
            bindingResult.reject("lancamento.invalido", ex.getMessage());
            prepararFormulario(model, TipoLancamento.SAIDA, false, null);
            return "caixa/saida";
        }
    }

    @GetMapping("/movimentacoes")
    public String movimentacoes(
            @ModelAttribute("filtro") LancamentoCaixaFiltro filtro,
            Model model) {
        model.addAttribute("paginaAtual", "caixa");
        model.addAttribute("categorias", categoriaService.listarTodas());
        try {
            model.addAttribute("lancamentos", lancamentoService.pesquisar(filtro));
            model.addAttribute("resumo", lancamentoService.calcularResumo(filtro));
        } catch (RegraNegocioException ex) {
            model.addAttribute("erroFiltro", ex.getMessage());
            model.addAttribute("lancamentos", List.of());
            model.addAttribute("resumo", lancamentoService.calcularResumo(new LancamentoCaixaFiltro()));
        }
        return "caixa/movimentacoes";
    }

    @GetMapping("/movimentacoes/{id}/editar")
    public String editarLancamento(@PathVariable Long id, Model model) {
        LancamentoCaixa lancamento = lancamentoService.buscarPorId(id);
        model.addAttribute("lancamentoId", id);
        model.addAttribute("lancamentoForm", LancamentoCaixaForm.from(lancamento));
        prepararFormulario(model, lancamento.getTipo(), true, lancamento.getCategoria());
        return "caixa/editar";
    }

    @GetMapping("/movimentacoes/{id}")
    public String detalharLancamento(@PathVariable Long id, Model model) {
        LancamentoCaixa lancamento = lancamentoService.buscarPorIdIncluindoCancelados(id);
        model.addAttribute("lancamento", lancamento);
        model.addAttribute(
                "historicoEntidade", historicoService.listarPorEntidade("LancamentoCaixa", id));
        model.addAttribute("paginaAtual", "movimentacoes");
        return "caixa/detalhes";
    }

    @GetMapping("/movimentacoes/{id}/excluir")
    public String confirmarExclusao(@PathVariable Long id, Model model, HttpSession sessao) {
        lancamentoService.buscarPorId(id);
        return mostrarRevisao(new OperacaoRevisaoDTO(
                TipoOperacaoRevisao.CANCELAR_LANCAMENTO, id), sessao, model);
    }

    @PostMapping("/movimentacoes/{id}/editar")
    public String atualizarLancamento(
            @PathVariable Long id,
            @Valid @ModelAttribute("lancamentoForm") LancamentoCaixaForm form,
            BindingResult bindingResult,
            Model model,
            HttpSession sessao) {
        LancamentoCaixa existente = lancamentoService.buscarPorId(id);
        form.setTipo(existente.getTipo());
        if (bindingResult.hasErrors()) {
            model.addAttribute("lancamentoId", id);
            prepararFormulario(model, existente.getTipo(), true, existente.getCategoria());
            return "caixa/editar";
        }
        try {
            return mostrarRevisao(new OperacaoRevisaoDTO(
                    TipoOperacaoRevisao.EDITAR_LANCAMENTO, id, form), sessao, model);
        } catch (RegraNegocioException | RecursoNaoEncontradoException ex) {
            bindingResult.reject("lancamento.invalido", ex.getMessage());
            model.addAttribute("lancamentoId", id);
            prepararFormulario(model, existente.getTipo(), true, existente.getCategoria());
            return "caixa/editar";
        }
    }

    @PostMapping("/movimentacoes/{id}/excluir")
    public String excluirLancamento(
            @PathVariable Long id,
            HttpSession sessao,
            Model model) {
        return mostrarRevisao(new OperacaoRevisaoDTO(
                TipoOperacaoRevisao.CANCELAR_LANCAMENTO, id), sessao, model);
    }

    @GetMapping("/categorias")
    public String categorias(Model model) {
        if (!model.containsAttribute("categoriaForm")) {
            model.addAttribute("categoriaForm", new CategoriaFinanceiraForm());
        }
        model.addAttribute("paginaAtual", "caixa");
        model.addAttribute("categorias", categoriaService.listarTodas());
        model.addAttribute("usosCategorias", categoriaService.resumirUsos());
        return "caixa/categorias";
    }

    @PostMapping("/categorias")
    public String cadastrarCategoria(
            @Valid @ModelAttribute("categoriaForm") CategoriaFinanceiraForm form,
            BindingResult bindingResult,
            Model model,
            HttpSession sessao) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("categorias", categoriaService.listarTodas());
            model.addAttribute("usosCategorias", categoriaService.resumirUsos());
            return "caixa/categorias";
        }
        try {
            return mostrarRevisao(new OperacaoRevisaoDTO(
                    TipoOperacaoRevisao.CRIAR_CATEGORIA, form), sessao, model);
        } catch (RegraNegocioException ex) {
            bindingResult.reject("categoria.invalida", ex.getMessage());
            model.addAttribute("categorias", categoriaService.listarTodas());
            model.addAttribute("usosCategorias", categoriaService.resumirUsos());
            return "caixa/categorias";
        }
    }

    @GetMapping("/categorias/{id}/editar")
    public String editarCategoria(@PathVariable Long id, Model model) {
        CategoriaFinanceira categoria = categoriaService.buscarPorId(id);
        model.addAttribute("categoriaForm", CategoriaFinanceiraForm.from(categoria));
        model.addAttribute("editandoId", id);
        model.addAttribute("categorias", categoriaService.listarTodas());
        model.addAttribute("usosCategorias", categoriaService.resumirUsos());
        return "caixa/categorias";
    }

    @PostMapping("/categorias/{id}/editar")
    public String atualizarCategoria(
            @PathVariable Long id,
            @Valid @ModelAttribute("categoriaForm") CategoriaFinanceiraForm form,
            BindingResult bindingResult,
            Model model,
            HttpSession sessao) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("editandoId", id);
            model.addAttribute("categorias", categoriaService.listarTodas());
            model.addAttribute("usosCategorias", categoriaService.resumirUsos());
            return "caixa/categorias";
        }
        try {
            return mostrarRevisao(new OperacaoRevisaoDTO(
                    TipoOperacaoRevisao.EDITAR_CATEGORIA, id, form), sessao, model);
        } catch (RegraNegocioException ex) {
            bindingResult.reject("categoria.invalida", ex.getMessage());
            model.addAttribute("editandoId", id);
            model.addAttribute("categorias", categoriaService.listarTodas());
            model.addAttribute("usosCategorias", categoriaService.resumirUsos());
            return "caixa/categorias";
        }
    }

    @PostMapping("/categorias/{id}/alternar-status")
    public String alternarStatusCategoria(
            @PathVariable Long id,
            HttpSession sessao,
            Model model) {
        return mostrarRevisao(new OperacaoRevisaoDTO(
                TipoOperacaoRevisao.ALTERNAR_CATEGORIA, id), sessao, model);
    }

    @GetMapping("/relatorio")
    public String relatorio(
            @ModelAttribute("filtro") LancamentoCaixaFiltro filtro,
            Model model) {
        model.addAttribute("lancamentos", lancamentoService.pesquisar(filtro));
        model.addAttribute("resumo", lancamentoService.calcularResumo(filtro));
        model.addAttribute("paginaAtual", "relatorios");
        return "relatorios/financeiro";
    }

    @GetMapping(value = "/relatorio.csv", produces = "text/csv")
    public ResponseEntity<byte[]> exportarCsv(@ModelAttribute LancamentoCaixaFiltro filtro) {
        String conteudo = '\uFEFF' + lancamentoService.gerarCsv(filtro);
        byte[] arquivo = conteudo.getBytes(StandardCharsets.UTF_8);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(new MediaType("text", "csv", StandardCharsets.UTF_8));
        headers.setContentDisposition(ContentDisposition.attachment().filename("livro-caixa.csv").build());
        return ResponseEntity.ok().headers(headers).body(arquivo);
    }

    private void adicionarResumoComTratamento(LancamentoCaixaFiltro filtro, Model model) {
        try {
            model.addAttribute("resumo", lancamentoService.calcularResumo(filtro));
        } catch (RegraNegocioException ex) {
            model.addAttribute("erroFiltro", ex.getMessage());
            ResumoFinanceiroDTO resumoSemFiltro = lancamentoService.calcularResumo(new LancamentoCaixaFiltro());
            model.addAttribute("resumo", resumoSemFiltro);
        }
    }

    private void prepararFormulario(
            Model model,
            TipoLancamento tipo,
            boolean edicao,
            CategoriaFinanceira categoriaAtual) {
        List<CategoriaFinanceira> categorias = new ArrayList<>(categoriaService.listarAtivas(tipo));
        if (categoriaAtual != null
                && categorias.stream().noneMatch(categoria -> categoria.getId().equals(categoriaAtual.getId()))) {
            categorias.add(categoriaAtual);
        }
        model.addAttribute("tipoLancamento", tipo);
        model.addAttribute("categorias", categorias);
        model.addAttribute("edicao", edicao);
        model.addAttribute("paginaAtual", "caixa");
    }

    private String mostrarRevisao(OperacaoRevisaoDTO operacao, HttpSession sessao, Model model) {
        RevisaoPreparadaDTO preparada = revisaoService.preparar(sessao, operacao);
        model.addAttribute("revisao", preparada.revisao());
        model.addAttribute("token", preparada.token());
        model.addAttribute("chave", revisaoService.chave(operacao));
        return "confirmacoes/revisao";
    }
}
