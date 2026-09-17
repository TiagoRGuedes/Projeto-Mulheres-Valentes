package com.ibmec.mulheresvalentes.service;

import com.ibmec.mulheresvalentes.dto.CategoriaFinanceiraForm;
import com.ibmec.mulheresvalentes.exception.RegraNegocioException;
import com.ibmec.mulheresvalentes.model.CategoriaFinanceira;
import com.ibmec.mulheresvalentes.model.TipoLancamento;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Transactional
class CategoriaFinanceiraServiceTest {

    @Autowired
    private CategoriaFinanceiraService categoriaService;

    @Autowired
    private LancamentoCaixaService lancamentoService;

    @Test
    void deveCriarEAlternarStatus() {
        CategoriaFinanceira categoria = categoriaService.cadastrar("  Materiais   de oficina  ", TipoLancamento.SAIDA);

        assertThat(categoria.getNome()).isEqualTo("Materiais de oficina");
        assertThat(categoria.isAtivo()).isTrue();
        assertThat(categoriaService.alternarStatus(categoria.getId()).isAtivo()).isFalse();
        assertThat(categoriaService.alternarStatus(categoria.getId()).isAtivo()).isTrue();
    }

    @Test
    void deveImpedirNomeDuplicadoNoMesmoTipo() {
        categoriaService.cadastrar("Transporte teste", TipoLancamento.SAIDA);

        assertThatThrownBy(() -> categoriaService.cadastrar("transporte teste", TipoLancamento.SAIDA))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("Já existe");
    }

    @Test
    void devePermitirMesmoNomeEmTiposDiferentes() {
        categoriaService.cadastrar("Outros teste", TipoLancamento.ENTRADA);
        categoriaService.cadastrar("Outros teste", TipoLancamento.SAIDA);

        assertThat(categoriaService.listarTodas())
                .filteredOn(categoria -> categoria.getNome().equals("Outros teste"))
                .hasSize(2);
    }

    @Test
    void naoDeveAlterarTipoDeCategoriaQuePossuiHistorico() {
        CategoriaFinanceira categoria = categoriaService.cadastrar("Categoria com histórico", TipoLancamento.ENTRADA);
        LancamentoCaixaFormBuilder.registrarEntrada(lancamentoService, categoria);
        CategoriaFinanceiraForm form = new CategoriaFinanceiraForm();
        form.setNome(categoria.getNome());
        form.setTipo(TipoLancamento.SAIDA);

        assertThatThrownBy(() -> categoriaService.editar(categoria.getId(), form))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("histórico");
    }

    private static final class LancamentoCaixaFormBuilder {
        private LancamentoCaixaFormBuilder() {
        }

        static void registrarEntrada(
                LancamentoCaixaService lancamentoService,
                CategoriaFinanceira categoria) {
            com.ibmec.mulheresvalentes.dto.LancamentoCaixaForm form =
                    new com.ibmec.mulheresvalentes.dto.LancamentoCaixaForm();
            form.setData(java.time.LocalDate.of(2026, 8, 1));
            form.setCategoriaId(categoria.getId());
            form.setDescricao("Entrada fictícia");
            form.setValor(new java.math.BigDecimal("10.00"));
            lancamentoService.registrarEntrada(form);
        }
    }
}
