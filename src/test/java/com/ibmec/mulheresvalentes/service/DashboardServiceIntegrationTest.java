package com.ibmec.mulheresvalentes.service;

import com.ibmec.mulheresvalentes.dto.DashboardResumoDTO;
import com.ibmec.mulheresvalentes.dto.LancamentoCaixaForm;
import com.ibmec.mulheresvalentes.model.Atividade;
import com.ibmec.mulheresvalentes.model.CategoriaFinanceira;
import com.ibmec.mulheresvalentes.model.Encontro;
import com.ibmec.mulheresvalentes.model.Participante;
import com.ibmec.mulheresvalentes.model.StatusPresenca;
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
class DashboardServiceIntegrationTest {

    @Autowired
    private DashboardService dashboardService;

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

    @Test
    void deveRetornarDashboardVazioComZerosEListasVazias() {
        DashboardResumoDTO dashboard = dashboardService.carregar();

        assertThat(dashboard.visaoGeral().participantesAtivos()).isZero();
        assertThat(dashboard.visaoGeral().atividadesAtivas()).isZero();
        assertThat(dashboard.visaoGeral().encontrosNoMes()).isZero();
        assertThat(dashboard.visaoGeral().registrosPresencaNoMes()).isZero();

        assertThat(dashboard.presenca().presentesNoMes()).isZero();
        assertThat(dashboard.presenca().ausentesNoMes()).isZero();
        assertThat(dashboard.presenca().justificadasNoMes()).isZero();
        assertThat(dashboard.presenca().frequenciaMedia()).isEqualByComparingTo("0.0");
        assertThat(dashboard.presenca().ultimoEncontroId()).isNull();
        assertThat(dashboard.presenca().percentualUltimoEncontro()).isEqualByComparingTo("0.0");

        assertThat(dashboard.financeiro().saldoAtual()).isEqualByComparingTo("0.00");
        assertThat(dashboard.financeiro().entradasNoMes()).isEqualByComparingTo("0.00");
        assertThat(dashboard.financeiro().saidasNoMes()).isEqualByComparingTo("0.00");
        assertThat(dashboard.financeiro().movimentacoesNoMes()).isZero();

        assertThat(dashboard.movimentacoesRecentes()).isEmpty();
        assertThat(dashboard.encontrosRecentes()).isEmpty();
        assertThat(dashboard.atividadeRecente()).isEmpty();
    }

    @Test
    void deveConsolidarMetricasPresencaCaixaEUltimoEncontro() {
        LocalDate hoje = LocalDate.now();
        LocalDate mesAnterior = hoje.minusMonths(1).withDayOfMonth(1);

        Participante presente = participanteService.cadastrar("Pessoa Presente Fictícia", null);
        Participante ausente = participanteService.cadastrar("Pessoa Ausente Fictícia", null);
        Participante justificada = participanteService.cadastrar("Pessoa Justificada Fictícia", null);
        Participante inativa = participanteService.cadastrar("Pessoa Inativa Fictícia", null);
        participanteService.desativar(inativa.getId());

        Atividade atividadeAtual = atividadeService.cadastrar("Atividade Atual Fictícia", null);
        Atividade atividadeInativa = atividadeService.cadastrar("Atividade Inativa Fictícia", null);
        atividadeService.desativar(atividadeInativa.getId());

        Encontro encontroAntigo = encontroService.cadastrar(
                atividadeAtual.getId(), mesAnterior, null);
        Encontro encontroAtual = encontroService.cadastrar(
                atividadeAtual.getId(), hoje, null);

        presencaService.registrar(
                presente.getId(), encontroAntigo.getId(), StatusPresenca.PRESENTE, null);
        presencaService.registrar(
                presente.getId(), encontroAtual.getId(), StatusPresenca.PRESENTE, null);
        presencaService.registrar(
                ausente.getId(), encontroAtual.getId(), StatusPresenca.AUSENTE, null);
        presencaService.registrar(
                justificada.getId(), encontroAtual.getId(), StatusPresenca.JUSTIFICADA, null);

        CategoriaFinanceira categoriaEntrada = categoriaService.cadastrar(
                "Receita Fictícia do Dashboard", TipoLancamento.ENTRADA);
        CategoriaFinanceira categoriaSaida = categoriaService.cadastrar(
                "Despesa Fictícia do Dashboard", TipoLancamento.SAIDA);
        lancamentoService.registrarEntrada(formulario(
                categoriaEntrada, "100.00", mesAnterior, "Entrada do mês anterior"));
        lancamentoService.registrarEntrada(formulario(
                categoriaEntrada, "200.00", hoje, "Entrada do mês atual"));
        lancamentoService.registrarSaida(formulario(
                categoriaSaida, "50.00", hoje, "Saída do mês atual"));
        var cancelado = lancamentoService.registrarEntrada(formulario(
                categoriaEntrada, "25.00", hoje, "Entrada cancelada"));
        lancamentoService.excluir(cancelado.getId());

        DashboardResumoDTO dashboard = dashboardService.carregar();

        assertThat(dashboard.visaoGeral().participantesAtivos()).isEqualTo(3);
        assertThat(dashboard.visaoGeral().atividadesAtivas()).isEqualTo(1);
        assertThat(dashboard.visaoGeral().encontrosNoMes()).isEqualTo(1);
        assertThat(dashboard.visaoGeral().registrosPresencaNoMes()).isEqualTo(3);

        assertThat(dashboard.presenca().presentesNoMes()).isEqualTo(1);
        assertThat(dashboard.presenca().ausentesNoMes()).isEqualTo(1);
        assertThat(dashboard.presenca().justificadasNoMes()).isEqualTo(1);
        assertThat(dashboard.presenca().frequenciaMedia()).isEqualByComparingTo("33.3");
        assertThat(dashboard.presenca().ultimoEncontroId()).isEqualTo(encontroAtual.getId());
        assertThat(dashboard.presenca().ultimaAtividade()).isEqualTo(atividadeAtual.getNome());
        assertThat(dashboard.presenca().dataUltimoEncontro()).isEqualTo(hoje);
        assertThat(dashboard.presenca().presentesUltimoEncontro()).isEqualTo(1);
        assertThat(dashboard.presenca().registrosUltimoEncontro()).isEqualTo(3);
        assertThat(dashboard.presenca().percentualUltimoEncontro()).isEqualByComparingTo("33.3");

        assertThat(dashboard.financeiro().entradasNoMes()).isEqualByComparingTo("200.00");
        assertThat(dashboard.financeiro().saidasNoMes()).isEqualByComparingTo("50.00");
        assertThat(dashboard.financeiro().saldoAtual()).isEqualByComparingTo("250.00");
        assertThat(dashboard.financeiro().movimentacoesNoMes()).isEqualTo(2);

        assertThat(dashboard.movimentacoesRecentes())
                .hasSize(3)
                .noneMatch(item -> item.getId().equals(cancelado.getId()));
        assertThat(dashboard.encontrosRecentes()).isNotEmpty();
        assertThat(dashboard.encontrosRecentes().getFirst().id()).isEqualTo(encontroAtual.getId());
        assertThat(dashboard.encontrosRecentes().getFirst().registrosPresenca()).isEqualTo(3);
        assertThat(dashboard.atividadeRecente()).hasSizeLessThanOrEqualTo(8).isNotEmpty();
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
        form.setObservacoes("Valor totalmente fictício para teste");
        return form;
    }
}
