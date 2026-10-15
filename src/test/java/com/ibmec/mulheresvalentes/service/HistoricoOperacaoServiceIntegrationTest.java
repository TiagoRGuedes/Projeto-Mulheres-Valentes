package com.ibmec.mulheresvalentes.service;

import com.ibmec.mulheresvalentes.dto.CategoriaFinanceiraForm;
import com.ibmec.mulheresvalentes.dto.LancamentoCaixaForm;
import com.ibmec.mulheresvalentes.dto.LancamentoCaixaRequest;
import com.ibmec.mulheresvalentes.exception.RegraNegocioException;
import com.ibmec.mulheresvalentes.model.AcaoHistorico;
import com.ibmec.mulheresvalentes.model.Atividade;
import com.ibmec.mulheresvalentes.model.CategoriaFinanceira;
import com.ibmec.mulheresvalentes.model.Encontro;
import com.ibmec.mulheresvalentes.model.HistoricoOperacao;
import com.ibmec.mulheresvalentes.model.LancamentoCaixa;
import com.ibmec.mulheresvalentes.model.NaturezaLancamento;
import com.ibmec.mulheresvalentes.model.ModuloHistorico;
import com.ibmec.mulheresvalentes.model.Participante;
import com.ibmec.mulheresvalentes.model.Presenca;
import com.ibmec.mulheresvalentes.model.StatusPresenca;
import com.ibmec.mulheresvalentes.model.TipoLancamento;
import com.ibmec.mulheresvalentes.repository.HistoricoOperacaoRepository;
import com.ibmec.mulheresvalentes.repository.LancamentoCaixaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Transactional
class HistoricoOperacaoServiceIntegrationTest {

    @Autowired
    private ParticipanteService participanteService;

    @Autowired
    private AtividadeService atividadeService;

    @Autowired
    private EncontroService encontroService;

    @Autowired
    private PresencaService presencaService;

    @Autowired
    private CategoriaFinanceiraService categoriaService;

    @Autowired
    private LancamentoCaixaService lancamentoService;

    @Autowired
    private HistoricoOperacaoService historicoService;

    @Autowired
    private HistoricoOperacaoRepository historicoRepository;

    @Autowired
    private LancamentoCaixaRepository lancamentoRepository;

    @Test
    void deveRegistrarCriacaoEdicaoEDesativacaoDeParticipante() {
        Participante participante = participanteService.cadastrar(
                "Participante Fictícia do Histórico", "(11) 90000-0000", "Observação de teste");
        participanteService.atualizar(
                participante.getId(), "Participante Fictícia Atualizada", "(11) 90000-0001", null);
        participanteService.desativar(participante.getId());

        List<HistoricoOperacao> historico = historicoService.listarPorEntidade(
                "Participante", participante.getId());

        assertThat(historico)
                .extracting(HistoricoOperacao::getAcao)
                .containsExactly(
                        AcaoHistorico.DESATIVACAO,
                        AcaoHistorico.EDICAO,
                        AcaoHistorico.CRIACAO);
        assertThat(historico)
                .allSatisfy(item -> {
                    assertThat(item.getModulo()).isEqualTo(ModuloHistorico.PARTICIPANTES);
                    assertThat(item.getUsuarioId()).isNull();
                    assertThat(item.getUsuarioNome()).isNull();
                    assertThat(item.getDataHora()).isNotNull();
                });
        HistoricoOperacao edicao = encontrar(historico, AcaoHistorico.EDICAO);
        assertThat(edicao.getValorAnteriorResumo()).contains("Participante Fictícia do Histórico");
        assertThat(edicao.getValorNovoResumo()).contains("Participante Fictícia Atualizada");
    }

    @Test
    void deveRegistrarCriacaoEAlteracoesDeAtividadeEEncontro() {
        Atividade atividade = atividadeService.cadastrar(
                "Atividade Fictícia Auditável", "Criada somente para teste");
        atividadeService.atualizar(
                atividade.getId(), "Atividade Fictícia Atualizada", null);
        atividadeService.desativar(atividade.getId());
        atividadeService.ativar(atividade.getId());

        Encontro encontro = encontroService.cadastrar(
                atividade.getId(), LocalDate.of(2026, 8, 20), "Encontro fictício");
        encontroService.atualizar(
                encontro.getId(), atividade.getId(), LocalDate.of(2026, 8, 21), null);

        assertThat(historicoService.listarPorEntidade("Atividade", atividade.getId()))
                .extracting(HistoricoOperacao::getAcao)
                .containsExactly(
                        AcaoHistorico.ATIVACAO,
                        AcaoHistorico.DESATIVACAO,
                        AcaoHistorico.EDICAO,
                        AcaoHistorico.CRIACAO);

        List<HistoricoOperacao> historicoEncontro = historicoService.listarPorEntidade(
                "Encontro", encontro.getId());
        assertThat(historicoEncontro)
                .extracting(HistoricoOperacao::getAcao)
                .containsExactly(AcaoHistorico.EDICAO, AcaoHistorico.CRIACAO);
        assertThat(encontrar(historicoEncontro, AcaoHistorico.EDICAO).getValorAnteriorResumo())
                .contains("2026-08-20");
        assertThat(encontrar(historicoEncontro, AcaoHistorico.EDICAO).getValorNovoResumo())
                .contains("2026-08-21");
    }

    @Test
    void deveRegistrarPresencaECorrecaoComSituacoesAnteriorENova() {
        Participante participante = participanteService.cadastrar("Participante Fictícia da Presença", null, null);
        Atividade atividade = atividadeService.cadastrar(
                "Oficina Fictícia da Presença", null);
        Encontro encontro = encontroService.cadastrar(
                atividade.getId(), LocalDate.of(2026, 8, 22), null);

        Presenca presenca = presencaService.registrarNoEncontro(
                participante.getId(), encontro.getId(), StatusPresenca.AUSENTE, null);
        presencaService.corrigir(
                presenca.getId(), StatusPresenca.JUSTIFICADA, "Justificativa fictícia");

        List<HistoricoOperacao> historico = historicoService.listarPorEntidade(
                "Presenca", presenca.getId());

        assertThat(historico)
                .extracting(HistoricoOperacao::getAcao)
                .containsExactly(AcaoHistorico.CORRECAO, AcaoHistorico.REGISTRO);
        HistoricoOperacao correcao = encontrar(historico, AcaoHistorico.CORRECAO);
        assertThat(correcao.getModulo()).isEqualTo(ModuloHistorico.PRESENCAS);
        assertThat(correcao.getValorAnteriorResumo()).contains("Ausente");
        assertThat(correcao.getValorNovoResumo()).contains("Ausência justificada");
    }

    @Test
    void deveRegistrarCriacaoEDesativacaoDeCategoria() {
        CategoriaFinanceira categoria = categoriaService.cadastrar(
                "Categoria Fictícia Auditável", TipoLancamento.SAIDA);
        categoriaService.alternarStatus(categoria.getId());

        List<HistoricoOperacao> historico = historicoService.listarPorEntidade(
                "CategoriaFinanceira", categoria.getId());

        assertThat(historico)
                .extracting(HistoricoOperacao::getAcao)
                .containsExactly(AcaoHistorico.DESATIVACAO, AcaoHistorico.CRIACAO);
        assertThat(historico)
                .allMatch(item -> item.getModulo() == ModuloHistorico.CATEGORIAS);
    }

    @Test
    void deveRegistrarEntradaSaidaEdicaoECancelamentoFinanceiro() {
        CategoriaFinanceira categoriaEntrada = categoriaService.cadastrar(
                "Entrada Fictícia Auditável", TipoLancamento.ENTRADA);
        CategoriaFinanceira categoriaSaida = categoriaService.cadastrar(
                "Saída Fictícia Auditável", TipoLancamento.SAIDA);

        LancamentoCaixa entrada = lancamentoService.registrarEntrada(formulario(
                categoriaEntrada, "100.00", LocalDate.of(2026, 8, 23), "Doação fictícia inicial"));
        LancamentoCaixa saida = lancamentoService.registrarSaida(formulario(
                categoriaSaida, "25.00", LocalDate.of(2026, 8, 24), "Despesa fictícia"));

        LancamentoCaixaRequest alteracao = new LancamentoCaixaRequest(
                LocalDate.of(2026, 8, 25),
                TipoLancamento.ENTRADA,
                NaturezaLancamento.MONETARIA,
                categoriaEntrada.getId(),
                "Doação fictícia corrigida",
                new BigDecimal("125.50"),
                null,
                null);
        lancamentoService.atualizar(entrada.getId(), alteracao);
        lancamentoService.excluir(saida.getId());

        List<HistoricoOperacao> historicoEntrada = historicoService.listarPorEntidade(
                "LancamentoCaixa", entrada.getId());
        assertThat(historicoEntrada)
                .extracting(HistoricoOperacao::getAcao)
                .containsExactly(AcaoHistorico.EDICAO, AcaoHistorico.REGISTRO);
        HistoricoOperacao edicao = encontrar(historicoEntrada, AcaoHistorico.EDICAO);
        assertThat(edicao.getValorAnteriorResumo())
                .contains("Doação fictícia inicial", "100,00");
        assertThat(edicao.getValorNovoResumo())
                .contains("Doação fictícia corrigida", "125,50");

        List<HistoricoOperacao> historicoSaida = historicoService.listarPorEntidade(
                "LancamentoCaixa", saida.getId());
        assertThat(historicoSaida)
                .extracting(HistoricoOperacao::getAcao)
                .containsExactly(AcaoHistorico.EXCLUSAO, AcaoHistorico.REGISTRO);
        HistoricoOperacao exclusao = encontrar(historicoSaida, AcaoHistorico.EXCLUSAO);
        assertThat(exclusao.getModulo()).isEqualTo(ModuloHistorico.LIVRO_CAIXA);
        assertThat(exclusao.getValorAnteriorResumo())
                .contains(
                        "Saída",
                        "2026-08-24",
                        "Saída Fictícia Auditável",
                        "Despesa fictícia",
                        "25,00");

        LancamentoCaixa preservado = lancamentoRepository.findById(saida.getId()).orElseThrow();
        assertThat(preservado.isCancelado()).isTrue();
        assertThat(preservado.getCanceladoEm()).isNotNull();
        assertThat(lancamentoService.pesquisar(null)).doesNotContain(preservado);
    }

    @Test
    void historicoDeveSerPesquisavelSemGuardarUsuarioFicticio() {
        participanteService.cadastrar("Pessoa Fictícia Pesquisável", null, null);

        var pagina = historicoService.pesquisar(
                LocalDate.now(), LocalDate.now(), ModuloHistorico.PARTICIPANTES,
                AcaoHistorico.CRIACAO, "PESSOA FICTÍCIA", 0);

        assertThat(pagina.getContent()).singleElement().satisfies(item -> {
            assertThat(item.getNomeEntidade()).isEqualTo("Pessoa Fictícia Pesquisável");
            assertThat(item.getUsuarioId()).isNull();
            assertThat(item.getUsuarioNome()).isNull();
        });
        assertThat(historicoRepository.count()).isPositive();
    }

    @Test
    void devePreservarHistoricoDepoisDeExclusoesSeguras() {
        Participante participante = participanteService.cadastrar("Pessoa Fictícia Sem Presença", null, null);
        Long participanteId = participante.getId();
        participanteService.excluir(participanteId);

        assertThat(participanteService.listar(null))
                .noneMatch(item -> item.getId().equals(participanteId));
        assertThat(historicoService.listarPorEntidade("Participante", participanteId))
                .extracting(HistoricoOperacao::getAcao)
                .containsExactly(AcaoHistorico.EXCLUSAO, AcaoHistorico.CRIACAO);

        Atividade atividade = atividadeService.cadastrar(
                "Atividade Fictícia Removível", null);
        Encontro encontro = encontroService.cadastrar(
                atividade.getId(), LocalDate.of(2026, 8, 26), null);
        Long encontroId = encontro.getId();
        Long atividadeId = atividade.getId();

        encontroService.excluir(encontroId);
        atividadeService.excluir(atividadeId);

        assertThat(historicoService.listarPorEntidade("Encontro", encontroId))
                .extracting(HistoricoOperacao::getAcao)
                .containsExactly(AcaoHistorico.EXCLUSAO, AcaoHistorico.CRIACAO);
        assertThat(historicoService.listarPorEntidade("Atividade", atividadeId))
                .extracting(HistoricoOperacao::getAcao)
                .containsExactly(AcaoHistorico.EXCLUSAO, AcaoHistorico.CRIACAO);
    }

    @Test
    void deveBloquearExclusaoQuandoHaHistoricoRelacionado() {
        Participante participante = participanteService.cadastrar("Pessoa Fictícia Protegida", null, null);
        Atividade atividade = atividadeService.cadastrar(
                "Atividade Fictícia Protegida", null);
        Encontro encontro = encontroService.cadastrar(
                atividade.getId(), LocalDate.of(2026, 8, 27), null);
        presencaService.registrarNoEncontro(
                participante.getId(), encontro.getId(), StatusPresenca.PRESENTE, null);

        assertThatThrownBy(() -> participanteService.excluir(participante.getId()))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("possui registros de presença");
        assertThatThrownBy(() -> encontroService.excluir(encontro.getId()))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("possui registros de presença");
        assertThatThrownBy(() -> atividadeService.excluir(atividade.getId()))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("possui encontros");
    }

    private HistoricoOperacao encontrar(
            List<HistoricoOperacao> historico,
            AcaoHistorico acao) {
        return historico.stream()
                .filter(item -> item.getAcao() == acao)
                .findFirst()
                .orElseThrow();
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
        form.setValor(new BigDecimal(valor));
        form.setObservacoes("Dado totalmente fictício para teste");
        return form;
    }

    @SuppressWarnings("unused")
    private CategoriaFinanceiraForm formularioCategoria(String nome, TipoLancamento tipo) {
        CategoriaFinanceiraForm form = new CategoriaFinanceiraForm();
        form.setNome(nome);
        form.setTipo(tipo);
        return form;
    }
}
