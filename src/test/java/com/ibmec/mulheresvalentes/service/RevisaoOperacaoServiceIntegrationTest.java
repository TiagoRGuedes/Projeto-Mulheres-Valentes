package com.ibmec.mulheresvalentes.service;

import com.ibmec.mulheresvalentes.dto.CategoriaFinanceiraForm;
import com.ibmec.mulheresvalentes.dto.LancamentoCaixaForm;
import com.ibmec.mulheresvalentes.dto.OperacaoRevisaoDTO;
import com.ibmec.mulheresvalentes.dto.ParticipanteForm;
import com.ibmec.mulheresvalentes.exception.RegraNegocioException;
import com.ibmec.mulheresvalentes.model.AcaoHistorico;
import com.ibmec.mulheresvalentes.model.TipoLancamento;
import com.ibmec.mulheresvalentes.model.TipoOperacaoRevisao;
import com.ibmec.mulheresvalentes.repository.HistoricoOperacaoRepository;
import com.ibmec.mulheresvalentes.repository.LancamentoCaixaRepository;
import com.ibmec.mulheresvalentes.repository.ParticipanteRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Transactional
class RevisaoOperacaoServiceIntegrationTest {

    @Autowired
    private RevisaoOperacaoService revisaoService;

    @Autowired
    private ParticipanteRepository participanteRepository;

    @Autowired
    private LancamentoCaixaRepository lancamentoRepository;

    @Autowired
    private HistoricoOperacaoRepository historicoRepository;

    @Autowired
    private CategoriaFinanceiraService categoriaService;

    @Autowired
    private LancamentoCaixaService lancamentoService;

    @Test
    void revisarEVoltarNaoDevemAlterarBancoNemHistorico() {
        long participantesAntes = participanteRepository.count();
        long historicoAntes = historicoRepository.count();
        ParticipanteForm form = participante("Pessoa da Revisão");
        MockHttpSession sessao = new MockHttpSession();

        var operacao = new OperacaoRevisaoDTO(TipoOperacaoRevisao.CRIAR_PARTICIPANTE, form);
        var preparada = revisaoService.preparar(sessao, operacao);

        assertThat(participanteRepository.count()).isEqualTo(participantesAntes);
        assertThat(historicoRepository.count()).isEqualTo(historicoAntes);

        revisaoService.cancelarRevisao(sessao, preparada.token(), revisaoService.chave(operacao));

        assertThat(participanteRepository.count()).isEqualTo(participantesAntes);
        assertThat(historicoRepository.count()).isEqualTo(historicoAntes);
    }

    @Test
    void confirmarUmaVezDeveGravarUmaVezEDuploPostDeveSerRejeitado() {
        long participantesAntes = participanteRepository.count();
        long historicoAntes = historicoRepository.count();
        MockHttpSession sessao = new MockHttpSession();
        var operacao = new OperacaoRevisaoDTO(
                TipoOperacaoRevisao.CRIAR_PARTICIPANTE, participante("Pessoa Uso Único"));
        var preparada = revisaoService.preparar(sessao, operacao);
        String chave = revisaoService.chave(operacao);

        revisaoService.confirmar(sessao, preparada.token(), chave);

        assertThat(participanteRepository.count()).isEqualTo(participantesAntes + 1);
        assertThat(historicoRepository.count()).isEqualTo(historicoAntes + 1);

        assertThatThrownBy(() -> revisaoService.confirmar(sessao, preparada.token(), chave))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("não é válida");
        assertThat(participanteRepository.count()).isEqualTo(participantesAntes + 1);
        assertThat(historicoRepository.count()).isEqualTo(historicoAntes + 1);
    }

    @Test
    void fluxoFinanceiroConfirmadoDeveRecalcularSaldoEManterRastreabilidade() {
        CategoriaFinanceiraForm categoriaForm = new CategoriaFinanceiraForm();
        categoriaForm.setNome("Entrada revisão exclusiva");
        categoriaForm.setTipo(TipoLancamento.ENTRADA);
        var categoria = categoriaService.cadastrar(categoriaForm);
        BigDecimal saldoInicial = lancamentoService.calcularSaldoAtual();
        MockHttpSession sessao = new MockHttpSession();

        LancamentoCaixaForm entrada = lancamento(categoria.getId(), "Entrada protegida", "500.00");
        var criar = new OperacaoRevisaoDTO(TipoOperacaoRevisao.REGISTRAR_ENTRADA, entrada);
        var revisaoCriacao = revisaoService.preparar(sessao, criar);
        assertThat(lancamentoService.calcularSaldoAtual()).isEqualByComparingTo(saldoInicial);
        var resultadoCriacao = revisaoService.confirmar(
                sessao, revisaoCriacao.token(), revisaoService.chave(criar));
        Long lancamentoId = Long.valueOf(resultadoCriacao.destino().substring(
                resultadoCriacao.destino().lastIndexOf('/') + 1));
        assertThat(lancamentoService.calcularSaldoAtual())
                .isEqualByComparingTo(saldoInicial.add(new BigDecimal("500.00")));

        LancamentoCaixaForm editada = lancamento(categoria.getId(), "Entrada protegida editada", "700.00");
        var editar = new OperacaoRevisaoDTO(
                TipoOperacaoRevisao.EDITAR_LANCAMENTO, lancamentoId, editada);
        var revisaoEdicao = revisaoService.preparar(sessao, editar);
        assertThat(lancamentoService.calcularSaldoAtual())
                .isEqualByComparingTo(saldoInicial.add(new BigDecimal("500.00")));
        revisaoService.confirmar(sessao, revisaoEdicao.token(), revisaoService.chave(editar));
        assertThat(lancamentoService.calcularSaldoAtual())
                .isEqualByComparingTo(saldoInicial.add(new BigDecimal("700.00")));

        var cancelar = new OperacaoRevisaoDTO(
                TipoOperacaoRevisao.CANCELAR_LANCAMENTO, lancamentoId);
        var revisaoCancelamento = revisaoService.preparar(sessao, cancelar);
        revisaoService.confirmar(
                sessao, revisaoCancelamento.token(), revisaoService.chave(cancelar));

        assertThat(lancamentoService.calcularSaldoAtual()).isEqualByComparingTo(saldoInicial);
        assertThat(lancamentoRepository.findById(lancamentoId)).isPresent()
                .get().extracting("cancelado").isEqualTo(true);
        assertThat(historicoRepository.findByTipoEntidadeAndEntidadeIdOrderByDataHoraDescIdDesc(
                        "LancamentoCaixa", lancamentoId))
                .extracting(item -> item.getAcao())
                .containsExactly(AcaoHistorico.EXCLUSAO, AcaoHistorico.EDICAO, AcaoHistorico.REGISTRO);
    }

    private ParticipanteForm participante(String nome) {
        ParticipanteForm form = new ParticipanteForm();
        form.setNome(nome);
        form.setObservacoes("Criado somente durante o teste.");
        return form;
    }

    private LancamentoCaixaForm lancamento(Long categoriaId, String descricao, String valor) {
        LancamentoCaixaForm form = new LancamentoCaixaForm();
        form.setData(LocalDate.now());
        form.setCategoriaId(categoriaId);
        form.setDescricao(descricao);
        form.setValor(new BigDecimal(valor));
        form.setTipo(TipoLancamento.ENTRADA);
        return form;
    }
}
