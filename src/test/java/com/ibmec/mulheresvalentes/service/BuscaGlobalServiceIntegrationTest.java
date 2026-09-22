package com.ibmec.mulheresvalentes.service;

import com.ibmec.mulheresvalentes.dto.GrupoResultadoBuscaDTO;
import com.ibmec.mulheresvalentes.dto.ItemResultadoBuscaDTO;
import com.ibmec.mulheresvalentes.dto.LancamentoCaixaForm;
import com.ibmec.mulheresvalentes.dto.ResultadoBuscaGlobalDTO;
import com.ibmec.mulheresvalentes.model.Atividade;
import com.ibmec.mulheresvalentes.model.CategoriaFinanceira;
import com.ibmec.mulheresvalentes.model.TipoLancamento;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Transactional
class BuscaGlobalServiceIntegrationTest {

    @Autowired
    private BuscaGlobalService buscaGlobalService;

    @Autowired
    private ParticipanteService participanteService;

    @Autowired
    private AtividadeService atividadeService;

    @Autowired
    private EncontroService encontroService;

    @Autowired
    private CategoriaFinanceiraService categoriaService;

    @Autowired
    private LancamentoCaixaService lancamentoService;

    @Test
    void deveRetornarResultadoVazioQuandoTermoNaoFoiInformado() {
        ResultadoBuscaGlobalDTO resultado = buscaGlobalService.buscar("   ");

        assertThat(resultado.pesquisaRealizada()).isFalse();
        assertThat(resultado.temResultados()).isFalse();
        assertThat(resultado.totalResultados()).isZero();
        assertThat(resultado.grupos()).isEmpty();
    }

    @Test
    void deveBuscarEAgruparRegistrosComLinksParaAsTelasCorretas() {
        var participante = participanteService.cadastrar("Ana Conexão Fictícia", null, null);
        Atividade atividade = atividadeService.cadastrar(
                "Oficina Conexão Fictícia", "Encontro de demonstração para a busca global");
        var encontro = encontroService.cadastrar(atividade.getId(), LocalDate.now(), null);
        CategoriaFinanceira categoria = categoriaService.cadastrar(
                "Conexão Solidária Fictícia", TipoLancamento.ENTRADA);
        var lancamento = lancamentoService.registrarEntrada(formulario(
                categoria, "Doação Conexão Fictícia", "125.50"));

        ResultadoBuscaGlobalDTO resultado = buscaGlobalService.buscar("  CONEXÃO  ");

        assertThat(resultado.termo()).isEqualTo("CONEXÃO");
        assertThat(resultado.grupos())
                .extracting(GrupoResultadoBuscaDTO::chave)
                .containsExactly("participantes", "atividades", "encontros", "financeiro", "categorias");
        assertThat(resultado.totalResultados()).isEqualTo(5);
        assertThat(urls(resultado))
                .contains(
                        "/participantes/" + participante.getId(),
                        "/atividades/" + atividade.getId(),
                        "/encontros/" + encontro.getId(),
                        "/caixa/movimentacoes/" + lancamento.getId(),
                        "/caixa/categorias");
    }

    @Test
    void deveLimitarCadaGrupoEIgnorarLancamentoCancelado() {
        for (int indice = 1; indice <= 8; indice++) {
            participanteService.cadastrar("Pessoa Limite Global " + indice, null, null);
        }
        CategoriaFinanceira categoria = categoriaService.cadastrar(
                "Entrada para cancelamento global", TipoLancamento.ENTRADA);
        var cancelado = lancamentoService.registrarEntrada(formulario(
                categoria, "Movimento Removido Especial", "80.00"));
        lancamentoService.excluir(cancelado.getId());

        ResultadoBuscaGlobalDTO participantes = buscaGlobalService.buscar("Limite Global");
        ResultadoBuscaGlobalDTO lancamentos = buscaGlobalService.buscar("Removido Especial");

        assertThat(participantes.grupos()).hasSize(1);
        assertThat(participantes.grupos().getFirst().itens())
                .hasSize(BuscaGlobalService.LIMITE_POR_GRUPO);
        assertThat(lancamentos.temResultados()).isFalse();
    }

    private java.util.List<String> urls(ResultadoBuscaGlobalDTO resultado) {
        return resultado.grupos().stream()
                .flatMap(grupo -> grupo.itens().stream())
                .map(ItemResultadoBuscaDTO::url)
                .toList();
    }

    private LancamentoCaixaForm formulario(
            CategoriaFinanceira categoria,
            String descricao,
            String valor) {
        LancamentoCaixaForm form = new LancamentoCaixaForm();
        form.setData(LocalDate.now());
        form.setCategoriaId(categoria.getId());
        form.setDescricao(descricao);
        form.setValor(new BigDecimal(valor));
        return form;
    }
}
