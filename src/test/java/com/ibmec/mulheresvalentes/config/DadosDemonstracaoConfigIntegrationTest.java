package com.ibmec.mulheresvalentes.config;

import com.ibmec.mulheresvalentes.model.NaturezaLancamento;
import com.ibmec.mulheresvalentes.model.Presenca;
import com.ibmec.mulheresvalentes.model.StatusPresenca;
import com.ibmec.mulheresvalentes.model.TipoLancamento;
import com.ibmec.mulheresvalentes.repository.AtividadeRepository;
import com.ibmec.mulheresvalentes.repository.CategoriaFinanceiraRepository;
import com.ibmec.mulheresvalentes.repository.EncontroRepository;
import com.ibmec.mulheresvalentes.repository.HistoricoOperacaoRepository;
import com.ibmec.mulheresvalentes.repository.LancamentoCaixaRepository;
import com.ibmec.mulheresvalentes.repository.ParticipanteRepository;
import com.ibmec.mulheresvalentes.repository.PresencaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
    "app.dados-demonstracao=true",
    "spring.datasource.url=jdbc:h2:mem:dados-demonstracao;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
@DirtiesContext
class DadosDemonstracaoConfigIntegrationTest {

    @Autowired
    private DadosDemonstracaoConfig dadosDemonstracaoConfig;

    @Autowired
    private ParticipanteRepository participanteRepository;

    @Autowired
    private AtividadeRepository atividadeRepository;

    @Autowired
    private EncontroRepository encontroRepository;

    @Autowired
    private PresencaRepository presencaRepository;

    @Autowired
    private CategoriaFinanceiraRepository categoriaRepository;

    @Autowired
    private LancamentoCaixaRepository lancamentoRepository;

    @Autowired
    private HistoricoOperacaoRepository historicoRepository;

    @Test
    void deveCriarCenarioAmploFicticioEIdempotente() throws Exception {
        assertThat(participanteRepository.count()).isEqualTo(36);
        assertThat(participanteRepository.countByAtivoTrue()).isEqualTo(32);
        assertThat(participanteRepository.countByAtivoFalse()).isEqualTo(4);
        assertThat(participanteRepository.findAll())
                .filteredOn(participante -> participante.getContato() != null)
                .isNotEmpty();

        assertThat(atividadeRepository.count()).isEqualTo(6);
        assertThat(atividadeRepository.countByAtivoTrue()).isEqualTo(5);
        assertThat(atividadeRepository.countByAtivoFalse()).isEqualTo(1);

        assertThat(encontroRepository.count()).isEqualTo(24);
        assertThat(encontroRepository.findAll()).allSatisfy(encontro ->
                assertThat(encontro.getData()).isBetween(LocalDate.now().minusMonths(4), LocalDate.now()));

        assertThat(presencaRepository.count()).isEqualTo(972);
        assertThat(presencaRepository.findAll())
                .filteredOn(presenca -> presenca.getEncontro() == null)
                .hasSize(108)
                .allSatisfy(presenca -> assertThat(presenca.getData()).isNotNull());
        assertThat(presencaRepository.findAll()).extracting(Presenca::getStatus)
                .contains(StatusPresenca.PRESENTE, StatusPresenca.AUSENTE, StatusPresenca.JUSTIFICADA);

        Map<Long, Long> presentesPorParticipante = presencaRepository.findAllComRelacionamentos().stream()
                .filter(presenca -> presenca.getStatus() == StatusPresenca.PRESENTE)
                .collect(Collectors.groupingBy(
                        presenca -> presenca.getParticipante().getId(),
                        Collectors.counting()));
        assertThat(Set.copyOf(presentesPorParticipante.values()))
                .containsExactlyInAnyOrder(13L, 17L, 20L, 22L, 24L, 26L);

        assertThat(categoriaRepository.count()).isEqualTo(13);
        assertThat(categoriaRepository.findByAtivoTrueOrderByTipoAscNomeAsc()).hasSize(11);

        assertThat(lancamentoRepository.count()).isEqualTo(30);
        assertThat(lancamentoRepository.findAll())
                .filteredOn(lancamento -> lancamento.getTipo() == TipoLancamento.ENTRADA)
                .hasSize(20);
        assertThat(lancamentoRepository.findAll())
                .filteredOn(lancamento -> lancamento.getTipo() == TipoLancamento.SAIDA)
                .hasSize(10);

        assertThat(lancamentoRepository.findAll())
                .filteredOn(lancamento -> lancamento.getNatureza() == NaturezaLancamento.MONETARIA)
                .hasSize(22)
                .allSatisfy(lancamento -> {
                    assertThat(lancamento.getValor()).isGreaterThan(BigDecimal.ZERO);
                    assertThat(lancamento.getValor().scale()).isEqualTo(2);
                });

        assertThat(lancamentoRepository.findAll())
                .filteredOn(lancamento -> lancamento.getNatureza() == NaturezaLancamento.DOACAO_MATERIAL)
                .hasSize(8)
                .allSatisfy(lancamento -> {
                    assertThat(lancamento.getValor()).isNull();
                    assertThat(lancamento.getComprovante()).isNotBlank();
                });

        assertThat(lancamentoRepository.findAll())
                .filteredOn(lancamento -> "Aluguel/Patrocínio".equals(lancamento.getCategoria().getNome()))
                .hasSize(12)
                .allSatisfy(lancamento ->
                        assertThat(lancamento.getValor()).isEqualByComparingTo(new BigDecimal("1200.00")));

        assertThat(historicoRepository.count()).isEqualTo(14);

        Contagens antes = contagensAtuais();
        dadosDemonstracaoConfig.run(new DefaultApplicationArguments(new String[0]));
        assertThat(contagensAtuais()).isEqualTo(antes);
    }

    private Contagens contagensAtuais() {
        return new Contagens(
                participanteRepository.count(),
                atividadeRepository.count(),
                encontroRepository.count(),
                presencaRepository.count(),
                categoriaRepository.count(),
                lancamentoRepository.count(),
                historicoRepository.count());
    }

    private record Contagens(
            long participantes,
            long atividades,
            long encontros,
            long presencas,
            long categorias,
            long lancamentos,
            long historicos) {
    }
}
