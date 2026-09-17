package com.ibmec.mulheresvalentes.service;

import com.ibmec.mulheresvalentes.dto.LancamentoCaixaFiltro;
import com.ibmec.mulheresvalentes.dto.LancamentoCaixaForm;
import com.ibmec.mulheresvalentes.dto.LancamentoCaixaRequest;
import com.ibmec.mulheresvalentes.dto.ResumoFinanceiroDTO;
import com.ibmec.mulheresvalentes.exception.RecursoNaoEncontradoException;
import com.ibmec.mulheresvalentes.exception.RegraNegocioException;
import com.ibmec.mulheresvalentes.model.CategoriaFinanceira;
import com.ibmec.mulheresvalentes.model.LancamentoCaixa;
import com.ibmec.mulheresvalentes.model.TipoLancamento;
import com.ibmec.mulheresvalentes.repository.LancamentoCaixaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Transactional
class LancamentoCaixaServiceTest {

    @Autowired
    private LancamentoCaixaService lancamentoService;

    @Autowired
    private CategoriaFinanceiraService categoriaService;

    @Autowired
    private LancamentoCaixaRepository lancamentoRepository;

    @Test
    void deveRegistrarEntradaComValorPositivoENormalizado() {
        CategoriaFinanceira categoria = categoriaService.cadastrar("Doação de teste", TipoLancamento.ENTRADA);
        LancamentoCaixaForm form = formulario(categoria, "1500.5", LocalDate.of(2026, 8, 10));

        LancamentoCaixa salvo = lancamentoService.registrarEntrada(form);

        assertThat(salvo.getId()).isNotNull();
        assertThat(salvo.getTipo()).isEqualTo(TipoLancamento.ENTRADA);
        assertThat(salvo.getValor()).isEqualByComparingTo("1500.50");
        assertThat(salvo.getValor().scale()).isEqualTo(2);
        assertThat(salvo.getRegistradoEm()).isNotNull();
        assertThat(salvo.getAtualizadoEm()).isNotNull();
    }

    @Test
    void deveSubtrairSaidaDoSaldo() {
        CategoriaFinanceira entrada = categoriaService.cadastrar("Entrada saldo", TipoLancamento.ENTRADA);
        CategoriaFinanceira saida = categoriaService.cadastrar("Saída saldo", TipoLancamento.SAIDA);
        lancamentoService.registrarEntrada(formulario(entrada, "100.00", LocalDate.of(2026, 8, 1)));
        lancamentoService.registrarSaida(formulario(saida, "35.25", LocalDate.of(2026, 8, 2)));

        ResumoFinanceiroDTO resumo = lancamentoService.calcularResumo(new LancamentoCaixaFiltro());

        assertThat(resumo.totalEntradas()).isEqualByComparingTo("100.00");
        assertThat(resumo.totalSaidas()).isEqualByComparingTo("35.25");
        assertThat(resumo.saldoPeriodo()).isEqualByComparingTo("64.75");
        assertThat(resumo.saldoAtual()).isEqualByComparingTo("64.75");
        assertThat(resumo.quantidadeMovimentacoes()).isEqualTo(2);
    }

    @Test
    void deveFiltrarPorPeriodoEManterSaldoAtualCompleto() {
        CategoriaFinanceira entrada = categoriaService.cadastrar("Entrada período", TipoLancamento.ENTRADA);
        CategoriaFinanceira saida = categoriaService.cadastrar("Saída período", TipoLancamento.SAIDA);
        lancamentoService.registrarEntrada(formulario(entrada, "500.00", LocalDate.of(2026, 7, 31)));
        lancamentoService.registrarEntrada(formulario(entrada, "200.00", LocalDate.of(2026, 8, 10)));
        lancamentoService.registrarSaida(formulario(saida, "45.00", LocalDate.of(2026, 8, 20)));
        lancamentoService.registrarSaida(formulario(saida, "10.00", LocalDate.of(2026, 9, 1)));

        LancamentoCaixaFiltro filtro = new LancamentoCaixaFiltro();
        filtro.setDataInicial(LocalDate.of(2026, 8, 1));
        filtro.setDataFinal(LocalDate.of(2026, 8, 31));
        ResumoFinanceiroDTO resumo = lancamentoService.calcularResumo(filtro);

        assertThat(resumo.totalEntradas()).isEqualByComparingTo("200.00");
        assertThat(resumo.totalSaidas()).isEqualByComparingTo("45.00");
        assertThat(resumo.saldoPeriodo()).isEqualByComparingTo("155.00");
        assertThat(resumo.saldoAtual()).isEqualByComparingTo("645.00");
        assertThat(resumo.quantidadeMovimentacoes()).isEqualTo(2);
    }

    @Test
    void deveCombinarFiltrosDeTipoCategoriaEDescricao() {
        CategoriaFinanceira doacao = categoriaService.cadastrar("Doação filtro", TipoLancamento.ENTRADA);
        CategoriaFinanceira evento = categoriaService.cadastrar("Evento filtro", TipoLancamento.ENTRADA);
        lancamentoService.registrarEntrada(formulario(doacao, "75.00", LocalDate.of(2026, 8, 1), "Doação mensal"));
        lancamentoService.registrarEntrada(formulario(doacao, "90.00", LocalDate.of(2026, 8, 2), "Campanha especial"));
        lancamentoService.registrarEntrada(formulario(evento, "120.00", LocalDate.of(2026, 8, 3), "Doação em evento"));

        LancamentoCaixaFiltro filtro = new LancamentoCaixaFiltro();
        filtro.setTipo(TipoLancamento.ENTRADA);
        filtro.setCategoriaId(doacao.getId());
        filtro.setDescricao("DOAÇÃO");

        assertThat(lancamentoService.pesquisar(filtro))
                .singleElement()
                .extracting(LancamentoCaixa::getValor)
                .isEqualTo(new BigDecimal("75.00"));
    }

    @Test
    void deveRejeitarValoresNulosZeroNegativosOuComMaisDeDuasCasas() {
        CategoriaFinanceira categoria = categoriaService.cadastrar("Entrada inválida", TipoLancamento.ENTRADA);

        assertThatThrownBy(() -> lancamentoService.registrarEntrada(formulario(categoria, null, LocalDate.now())))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessage("Informe um valor maior que zero.");
        assertThatThrownBy(() -> lancamentoService.registrarEntrada(formulario(categoria, "0.00", LocalDate.now())))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessage("Informe um valor maior que zero.");
        assertThatThrownBy(() -> lancamentoService.registrarEntrada(formulario(categoria, "-1.00", LocalDate.now())))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessage("Informe um valor maior que zero.");
        assertThatThrownBy(() -> lancamentoService.registrarEntrada(formulario(categoria, "1.001", LocalDate.now())))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("duas casas decimais");
    }

    @Test
    void deveSomarBigDecimalSemErroDePontoFlutuante() {
        CategoriaFinanceira categoria = categoriaService.cadastrar("Precisão decimal", TipoLancamento.ENTRADA);
        lancamentoService.registrarEntrada(formulario(categoria, "0.10", LocalDate.of(2026, 8, 1)));
        lancamentoService.registrarEntrada(formulario(categoria, "0.20", LocalDate.of(2026, 8, 2)));

        assertThat(lancamentoService.calcularSaldoAtual())
                .isEqualTo(new BigDecimal("0.30"));
    }

    @Test
    void deveRejeitarCategoriaDeTipoDiferente() {
        CategoriaFinanceira categoriaSaida = categoriaService.cadastrar("Categoria de saída", TipoLancamento.SAIDA);

        assertThatThrownBy(() -> lancamentoService.registrarEntrada(
                formulario(categoriaSaida, "20.00", LocalDate.now())))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("categoria de entrada");
    }

    @Test
    void deveValidarOrdemDoPeriodo() {
        LancamentoCaixaFiltro filtro = new LancamentoCaixaFiltro();
        filtro.setDataInicial(LocalDate.of(2026, 8, 31));
        filtro.setDataFinal(LocalDate.of(2026, 8, 1));

        assertThatThrownBy(() -> lancamentoService.pesquisar(filtro))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("data inicial");
    }

    @Test
    void deveAtualizarEExcluirLancamento() {
        CategoriaFinanceira categoria = categoriaService.cadastrar("CRUD financeiro", TipoLancamento.ENTRADA);
        LancamentoCaixa salvo = lancamentoService.registrarEntrada(
                formulario(categoria, "10.00", LocalDate.of(2026, 8, 1)));

        LancamentoCaixaRequest alteracao = new LancamentoCaixaRequest(
                LocalDate.of(2026, 8, 2), TipoLancamento.ENTRADA, categoria.getId(),
                "Valor corrigido", new BigDecimal("12.50"), null);
        LancamentoCaixa atualizado = lancamentoService.atualizar(salvo.getId(), alteracao);

        assertThat(atualizado.getDescricao()).isEqualTo("Valor corrigido");
        assertThat(atualizado.getValor()).isEqualByComparingTo("12.50");
        lancamentoService.excluir(salvo.getId());
        assertThatThrownBy(() -> lancamentoService.buscarPorId(salvo.getId()))
                .isInstanceOf(RecursoNaoEncontradoException.class);
        assertThat(lancamentoService.buscarPorIdIncluindoCancelados(salvo.getId()))
                .satisfies(cancelado -> {
                    assertThat(cancelado.isCancelado()).isTrue();
                    assertThat(cancelado.getCanceladoEm()).isNotNull();
                });
        assertThat(lancamentoRepository.findById(salvo.getId()))
                .get()
                .satisfies(cancelado -> {
                    assertThat(cancelado.isCancelado()).isTrue();
                    assertThat(cancelado.getCanceladoEm()).isNotNull();
                });
        assertThat(lancamentoService.calcularSaldoAtual()).isEqualByComparingTo("0.00");
    }

    private LancamentoCaixaForm formulario(
            CategoriaFinanceira categoria,
            String valor,
            LocalDate data) {
        return formulario(categoria, valor, data, "Lançamento de teste");
    }

    private LancamentoCaixaForm formulario(
            CategoriaFinanceira categoria,
            String valor,
            LocalDate data,
            String descricao) {
        LancamentoCaixaForm form = new LancamentoCaixaForm();
        form.setData(data);
        form.setCategoriaId(categoria.getId());
        form.setDescricao(descricao);
        form.setValor(valor == null ? null : new BigDecimal(valor));
        form.setObservacoes("Dado totalmente fictício para teste");
        return form;
    }
}
