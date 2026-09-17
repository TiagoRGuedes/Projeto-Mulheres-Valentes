package com.ibmec.mulheresvalentes.service;

import com.ibmec.mulheresvalentes.dto.LancamentoCaixaFiltro;
import com.ibmec.mulheresvalentes.dto.LancamentoCaixaForm;
import com.ibmec.mulheresvalentes.dto.LancamentoCaixaRequest;
import com.ibmec.mulheresvalentes.dto.LancamentoCaixaResponse;
import com.ibmec.mulheresvalentes.dto.ResumoFinanceiroDTO;
import com.ibmec.mulheresvalentes.exception.RecursoNaoEncontradoException;
import com.ibmec.mulheresvalentes.exception.RegraNegocioException;
import com.ibmec.mulheresvalentes.model.CategoriaFinanceira;
import com.ibmec.mulheresvalentes.model.AcaoHistorico;
import com.ibmec.mulheresvalentes.model.LancamentoCaixa;
import com.ibmec.mulheresvalentes.model.ModuloHistorico;
import com.ibmec.mulheresvalentes.model.TipoLancamento;
import com.ibmec.mulheresvalentes.repository.LancamentoCaixaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

@Service
public class LancamentoCaixaService {

    private static final BigDecimal ZERO_MOEDA = new BigDecimal("0.00");
    private static final int MAXIMO_CASAS_INTEIRAS = 8;
    private static final int MAXIMO_CASAS_DECIMAIS = 2;

    private final LancamentoCaixaRepository lancamentoRepository;
    private final CategoriaFinanceiraService categoriaService;
    private final HistoricoOperacaoService historicoService;
    private final FormatadorService formatadorService;

    public LancamentoCaixaService(
            LancamentoCaixaRepository lancamentoRepository,
            CategoriaFinanceiraService categoriaService,
            HistoricoOperacaoService historicoService,
            FormatadorService formatadorService) {
        this.lancamentoRepository = lancamentoRepository;
        this.categoriaService = categoriaService;
        this.historicoService = historicoService;
        this.formatadorService = formatadorService;
    }

    @Transactional
    public LancamentoCaixa registrarEntrada(LancamentoCaixaForm form) {
        return registrar(TipoLancamento.ENTRADA, form);
    }

    @Transactional
    public LancamentoCaixa registrarSaida(LancamentoCaixaForm form) {
        return registrar(TipoLancamento.SAIDA, form);
    }

    @Transactional
    public LancamentoCaixa registrar(TipoLancamento tipo, LancamentoCaixaForm form) {
        if (form == null) {
            throw new RegraNegocioException("Informe os dados do lançamento.");
        }
        return criarNovo(
                form.getData(), tipo, form.getCategoriaId(), form.getDescricao(),
                form.getValor(), form.getObservacoes());
    }

    @Transactional
    public LancamentoCaixa criar(LancamentoCaixaRequest request) {
        if (request == null) {
            throw new RegraNegocioException("Informe os dados do lançamento.");
        }
        return criarNovo(
                request.data(), request.tipo(), request.categoriaId(), request.descricao(),
                request.valor(), request.observacoes());
    }

    @Transactional(readOnly = true)
    public LancamentoCaixa buscarPorId(Long id) {
        if (id == null) {
            throw new RegraNegocioException("Informe o lançamento.");
        }
        return lancamentoRepository.findByIdAndCanceladoFalse(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Lançamento não encontrado."));
    }

    /**
     * Consulta usada pela trilha de auditoria. Inclui lançamentos cancelados,
     * que permanecem preservados e são exibidos apenas em modo de leitura.
     */
    @Transactional(readOnly = true)
    public LancamentoCaixa buscarPorIdIncluindoCancelados(Long id) {
        if (id == null) {
            throw new RegraNegocioException("Informe o lançamento.");
        }
        return lancamentoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Lançamento não encontrado."));
    }

    @Transactional(readOnly = true)
    public LancamentoCaixaResponse buscarRespostaPorId(Long id) {
        return LancamentoCaixaResponse.from(buscarPorId(id));
    }

    @Transactional(readOnly = true)
    public List<LancamentoCaixa> pesquisar(LancamentoCaixaFiltro filtro) {
        LancamentoCaixaFiltro seguro = filtro == null ? new LancamentoCaixaFiltro() : filtro;
        validarPeriodo(seguro.getDataInicial(), seguro.getDataFinal());
        return lancamentoRepository.pesquisar(
                seguro.getDataInicial(),
                seguro.getDataFinal(),
                seguro.getTipo(),
                seguro.getCategoriaId(),
                seguro.getDescricaoNormalizada());
    }

    @Transactional(readOnly = true)
    public List<LancamentoCaixaResponse> pesquisarRespostas(LancamentoCaixaFiltro filtro) {
        return pesquisar(filtro).stream().map(LancamentoCaixaResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<LancamentoCaixa> listarRecentes() {
        return lancamentoRepository.findTop5ByCanceladoFalseOrderByDataDescIdDesc();
    }

    @Transactional(readOnly = true)
    public ResumoFinanceiroDTO calcularResumo(LancamentoCaixaFiltro filtro) {
        LancamentoCaixaFiltro seguro = filtro == null ? new LancamentoCaixaFiltro() : filtro;
        List<LancamentoCaixa> lancamentosDoPeriodo = pesquisar(seguro);

        BigDecimal entradas = somar(lancamentosDoPeriodo, TipoLancamento.ENTRADA);
        BigDecimal saidas = somar(lancamentosDoPeriodo, TipoLancamento.SAIDA);
        BigDecimal saldoPeriodo = entradas.subtract(saidas).setScale(2, RoundingMode.UNNECESSARY);
        BigDecimal saldoAtual = calcularSaldoAtual();

        return new ResumoFinanceiroDTO(
                seguro.getDataInicial(),
                seguro.getDataFinal(),
                entradas,
                saidas,
                saldoPeriodo,
                saldoAtual,
                lancamentosDoPeriodo.size());
    }

    @Transactional(readOnly = true)
    public BigDecimal calcularSaldoAtual() {
        BigDecimal entradas = lancamentoRepository.somarAtivosPorTipoEPeriodo(
                TipoLancamento.ENTRADA, null, null);
        BigDecimal saidas = lancamentoRepository.somarAtivosPorTipoEPeriodo(
                TipoLancamento.SAIDA, null, null);
        return valorSeguro(entradas)
                .subtract(valorSeguro(saidas))
                .setScale(2, RoundingMode.UNNECESSARY);
    }

    @Transactional
    public LancamentoCaixa atualizar(Long id, LancamentoCaixaRequest request) {
        if (request == null) {
            throw new RegraNegocioException("Informe os dados do lançamento.");
        }
        LancamentoCaixa lancamento = buscarPorId(id);
        return atualizarExistente(
                lancamento,
                request.data(), request.tipo(), request.categoriaId(), request.descricao(),
                request.valor(), request.observacoes());
    }

    @Transactional
    public LancamentoCaixa atualizar(Long id, LancamentoCaixaForm form) {
        if (form == null) {
            throw new RegraNegocioException("Informe os dados do lançamento.");
        }
        LancamentoCaixa lancamento = buscarPorId(id);
        return atualizarExistente(
                lancamento,
                form.getData(), lancamento.getTipo(), form.getCategoriaId(), form.getDescricao(),
                form.getValor(), form.getObservacoes());
    }

    @Transactional
    public void excluir(Long id) {
        LancamentoCaixa lancamento = buscarPorId(id);
        String resumo = resumoLancamento(lancamento);
        lancamento.cancelar();
        lancamentoRepository.save(lancamento);
        historicoService.registrar(
                ModuloHistorico.LIVRO_CAIXA,
                "LancamentoCaixa",
                lancamento.getId(),
                lancamento.getDescricao(),
                AcaoHistorico.EXCLUSAO,
                "Lançamento cancelado. Ele foi retirado do saldo e dos relatórios, mas preservado para rastreabilidade.",
                resumo,
                "Situação: cancelado em " + lancamento.getCanceladoEm());
    }

    @Transactional(readOnly = true)
    public String gerarCsv(LancamentoCaixaFiltro filtro) {
        List<LancamentoCaixa> lancamentos = pesquisar(filtro);
        ResumoFinanceiroDTO resumo = calcularResumo(filtro);
        DecimalFormat numero = new DecimalFormat("0.00", DecimalFormatSymbols.getInstance(Locale.of("pt", "BR")));

        StringBuilder csv = new StringBuilder();
        csv.append("Data;Tipo;Categoria;Descrição;Valor;Observações\r\n");
        for (LancamentoCaixa lancamento : lancamentos) {
            csv.append(campoCsv(lancamento.getData().toString())).append(';')
                    .append(campoCsv(lancamento.getTipo().getDescricao())).append(';')
                    .append(campoCsv(lancamento.getCategoria().getNome())).append(';')
                    .append(campoCsv(lancamento.getDescricao())).append(';')
                    .append(campoCsv(numero.format(lancamento.getValor()))).append(';')
                    .append(campoCsv(lancamento.getObservacoes())).append("\r\n");
        }
        csv.append("\r\n")
                .append("Resumo do período;;;;;\r\n")
                .append("Total de entradas;").append(campoCsv(numero.format(resumo.totalEntradas()))).append(";;;;\r\n")
                .append("Total de saídas;").append(campoCsv(numero.format(resumo.totalSaidas()))).append(";;;;\r\n")
                .append("Saldo do período;").append(campoCsv(numero.format(resumo.saldoPeriodo()))).append(";;;;\r\n");
        return csv.toString();
    }

    private LancamentoCaixa criarNovo(
            LocalDate data,
            TipoLancamento tipo,
            Long categoriaId,
            String descricao,
            BigDecimal valor,
            String observacoes) {
        validarCampos(data, tipo, descricao, valor, observacoes);
        CategoriaFinanceira categoria = categoriaService.buscarAtivaDoTipo(categoriaId, tipo);
        LancamentoCaixa lancamento = new LancamentoCaixa(
                data, tipo, categoria, normalizarDescricao(descricao),
                normalizarValor(valor), normalizarObservacoes(observacoes));
        LancamentoCaixa salvo = lancamentoRepository.save(lancamento);
        String nomeAcao = tipo == TipoLancamento.ENTRADA ? "Entrada registrada" : "Saída registrada";
        historicoService.registrar(
                ModuloHistorico.LIVRO_CAIXA,
                "LancamentoCaixa",
                salvo.getId(),
                salvo.getDescricao(),
                AcaoHistorico.REGISTRO,
                nomeAcao + ": " + formatadorService.moeda(salvo.getValor()) + ".",
                null,
                resumoLancamento(salvo));
        return salvo;
    }

    private LancamentoCaixa atualizarExistente(
            LancamentoCaixa lancamento,
            LocalDate data,
            TipoLancamento tipo,
            Long categoriaId,
            String descricao,
            BigDecimal valor,
            String observacoes) {
        String anterior = resumoLancamento(lancamento);
        validarCampos(data, tipo, descricao, valor, observacoes);
        CategoriaFinanceira categoria;
        if (lancamento.getCategoria().getId().equals(categoriaId)
                && lancamento.getCategoria().getTipo() == tipo) {
            categoria = lancamento.getCategoria();
        } else {
            categoria = categoriaService.buscarAtivaDoTipo(categoriaId, tipo);
        }

        lancamento.setData(data);
        lancamento.setTipo(tipo);
        lancamento.setCategoria(categoria);
        lancamento.setDescricao(normalizarDescricao(descricao));
        lancamento.setValor(normalizarValor(valor));
        lancamento.setObservacoes(normalizarObservacoes(observacoes));
        LancamentoCaixa salvo = lancamentoRepository.save(lancamento);
        historicoService.registrar(
                ModuloHistorico.LIVRO_CAIXA,
                "LancamentoCaixa",
                salvo.getId(),
                salvo.getDescricao(),
                AcaoHistorico.EDICAO,
                "Lançamento atualizado.",
                anterior,
                resumoLancamento(salvo));
        return salvo;
    }

    private void validarCampos(
            LocalDate data,
            TipoLancamento tipo,
            String descricao,
            BigDecimal valor,
            String observacoes) {
        if (data == null) {
            throw new RegraNegocioException("Informe a data do lançamento.");
        }
        if (tipo == null) {
            throw new RegraNegocioException("Informe se o lançamento é uma entrada ou saída.");
        }
        if (!StringUtils.hasText(descricao)) {
            throw new RegraNegocioException("Informe uma descrição.");
        }
        if (normalizarDescricao(descricao).length() > 160) {
            throw new RegraNegocioException("A descrição deve ter no máximo 160 caracteres.");
        }
        if (observacoes != null && observacoes.trim().length() > 500) {
            throw new RegraNegocioException("A observação deve ter no máximo 500 caracteres.");
        }
        validarValor(valor);
    }

    private void validarValor(BigDecimal valor) {
        if (valor == null || valor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RegraNegocioException("Informe um valor maior que zero.");
        }
        if (valor.scale() > MAXIMO_CASAS_DECIMAIS) {
            throw new RegraNegocioException("Informe o valor com no máximo duas casas decimais.");
        }
        int casasInteiras = Math.max(0, valor.precision() - valor.scale());
        if (casasInteiras > MAXIMO_CASAS_INTEIRAS) {
            throw new RegraNegocioException("O valor deve ter no máximo 8 dígitos antes da vírgula.");
        }
    }

    private void validarPeriodo(LocalDate dataInicial, LocalDate dataFinal) {
        if (dataInicial != null && dataFinal != null && dataInicial.isAfter(dataFinal)) {
            throw new RegraNegocioException("A data inicial deve ser anterior ou igual à data final.");
        }
    }

    private BigDecimal somar(List<LancamentoCaixa> lancamentos, TipoLancamento tipo) {
        return lancamentos.stream()
                .filter(lancamento -> lancamento.getTipo() == tipo)
                .map(LancamentoCaixa::getValor)
                .reduce(ZERO_MOEDA, BigDecimal::add)
                .setScale(2, RoundingMode.UNNECESSARY);
    }

    private BigDecimal normalizarValor(BigDecimal valor) {
        return valor.setScale(2, RoundingMode.UNNECESSARY);
    }

    private BigDecimal valorSeguro(BigDecimal valor) {
        return valor == null ? ZERO_MOEDA : valor.setScale(2, RoundingMode.UNNECESSARY);
    }

    private String resumoLancamento(LancamentoCaixa lancamento) {
        return lancamento.getTipo().getDescricao()
                + "; data: " + lancamento.getData()
                + "; categoria: " + lancamento.getCategoria().getNome()
                + "; descrição: " + lancamento.getDescricao()
                + "; valor: " + formatadorService.moeda(lancamento.getValor());
    }

    private String normalizarDescricao(String descricao) {
        return descricao.trim().replaceAll("\\s+", " ");
    }

    private String normalizarObservacoes(String observacoes) {
        if (!StringUtils.hasText(observacoes)) {
            return null;
        }
        return observacoes.trim().replaceAll("\\s+", " ");
    }

    private String campoCsv(String conteudo) {
        if (conteudo == null) {
            return "\"\"";
        }
        String seguro = conteudo;
        if (!seguro.isEmpty() && "=+-@".indexOf(seguro.charAt(0)) >= 0) {
            seguro = "'" + seguro;
        }
        return '"' + seguro.replace("\"", "\"\"") + '"';
    }
}
