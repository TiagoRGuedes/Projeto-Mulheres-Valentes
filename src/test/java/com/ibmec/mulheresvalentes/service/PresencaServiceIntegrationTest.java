package com.ibmec.mulheresvalentes.service;

import com.ibmec.mulheresvalentes.dto.FrequenciaParticipanteDto;
import com.ibmec.mulheresvalentes.dto.ItemRegistroPresencaForm;
import com.ibmec.mulheresvalentes.dto.RegistroPresencaForm;
import com.ibmec.mulheresvalentes.dto.ResumoRegistroPresencaDto;
import com.ibmec.mulheresvalentes.exception.RegistroDuplicadoException;
import com.ibmec.mulheresvalentes.exception.RegraNegocioException;
import com.ibmec.mulheresvalentes.model.Atividade;
import com.ibmec.mulheresvalentes.model.Encontro;
import com.ibmec.mulheresvalentes.model.Participante;
import com.ibmec.mulheresvalentes.model.StatusPresenca;
import com.ibmec.mulheresvalentes.repository.PresencaRepository;
import org.junit.jupiter.api.BeforeEach;
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
class PresencaServiceIntegrationTest {

    @Autowired
    private ParticipanteService participanteService;

    @Autowired
    private AtividadeService atividadeService;

    @Autowired
    private EncontroService encontroService;

    @Autowired
    private PresencaService presencaService;

    @Autowired
    private PresencaRepository presencaRepository;

    private Participante ana;
    private Participante bia;
    private Atividade atividade;
    private Encontro encontroUm;
    private Encontro encontroDois;

    @BeforeEach
    void prepararCenario() {
        ana = participanteService.cadastrar("Ana Exemplo", null);
        bia = participanteService.cadastrar("Bia Exemplo", null);
        atividade = atividadeService.cadastrar("Atividade fictícia", "Somente para teste");
        encontroUm = encontroService.cadastrar(
                atividade.getId(), LocalDate.of(2026, 8, 10), null
        );
        encontroDois = encontroService.cadastrar(
                atividade.getId(), LocalDate.of(2026, 8, 17), null
        );
    }

    @Test
    void deveRegistrarPresencaIndividual() {
        var presenca = presencaService.registrar(
                ana.getId(), encontroUm.getId(), StatusPresenca.PRESENTE, "Chegou no horário"
        );

        assertThat(presenca.getId()).isNotNull();
        assertThat(presenca.getStatus()).isEqualTo(StatusPresenca.PRESENTE);
        assertThat(presencaRepository.existsByParticipanteIdAndEncontroId(
                ana.getId(), encontroUm.getId()
        )).isTrue();
    }

    @Test
    void deveImpedirDuplicidadeDeParticipanteEEncontro() {
        presencaService.registrar(
                ana.getId(), encontroUm.getId(), StatusPresenca.PRESENTE, null
        );

        assertThatThrownBy(() -> presencaService.registrar(
                ana.getId(), encontroUm.getId(), StatusPresenca.AUSENTE, null
        ))
                .isInstanceOf(RegistroDuplicadoException.class)
                .hasMessage("Já existe uma presença registrada para este participante nesse encontro.");
    }

    @Test
    void deveSalvarTodosJuntosEPermitirCorrecaoSemDuplicar() {
        RegistroPresencaForm primeiroRegistro = formulario(
                encontroUm,
                item(ana, StatusPresenca.PRESENTE),
                item(bia, StatusPresenca.AUSENTE)
        );

        ResumoRegistroPresencaDto resumo = presencaService.salvarRegistroColetivo(primeiroRegistro);

        assertThat(resumo.total()).isEqualTo(2);
        assertThat(resumo.presentes()).isEqualTo(1);
        assertThat(resumo.ausentes()).isEqualTo(1);
        assertThat(presencaRepository.count()).isEqualTo(2);

        RegistroPresencaForm correcao = formulario(
                encontroUm,
                item(ana, StatusPresenca.PRESENTE),
                item(bia, StatusPresenca.JUSTIFICADA)
        );
        presencaService.salvarRegistroColetivo(correcao);

        assertThat(presencaRepository.count()).isEqualTo(2);
        assertThat(presencaRepository.findByParticipanteIdAndEncontroId(
                bia.getId(), encontroUm.getId()
        )).get().extracting(p -> p.getStatus()).isEqualTo(StatusPresenca.JUSTIFICADA);
    }

    @Test
    void deveRejeitarParticipanteRepetidoNoMesmoFormulario() {
        RegistroPresencaForm formulario = formulario(
                encontroUm,
                item(ana, StatusPresenca.PRESENTE),
                item(ana, StatusPresenca.AUSENTE)
        );

        assertThatThrownBy(() -> presencaService.salvarRegistroColetivo(formulario))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessage("Um participante foi enviado mais de uma vez.");
    }

    @Test
    void deveCancelarTodoLoteQuandoUmItemForInvalido() {
        participanteService.desativar(bia.getId());
        RegistroPresencaForm formulario = formulario(
                encontroUm,
                item(ana, StatusPresenca.PRESENTE),
                item(bia, StatusPresenca.AUSENTE)
        );

        assertThatThrownBy(() -> presencaService.salvarRegistroColetivo(formulario))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("está inativo");
        assertThat(presencaRepository.count()).isZero();
    }

    @Test
    void deveCalcularFrequenciaEmJavaComArredondamento() {
        Encontro encontroTres = encontroService.cadastrar(
                atividade.getId(), LocalDate.of(2026, 8, 24), null
        );
        presencaService.registrar(ana.getId(), encontroUm.getId(), StatusPresenca.PRESENTE, null);
        presencaService.registrar(ana.getId(), encontroDois.getId(), StatusPresenca.AUSENTE, null);
        presencaService.registrar(ana.getId(), encontroTres.getId(), StatusPresenca.JUSTIFICADA, null);
        presencaService.registrar(bia.getId(), encontroUm.getId(), StatusPresenca.JUSTIFICADA, null);

        List<FrequenciaParticipanteDto> frequencias = presencaService.calcularFrequencias(
                atividade.getId(), null, null, null
        );

        FrequenciaParticipanteDto frequenciaAna = frequencias.stream()
                .filter(item -> item.participanteId().equals(ana.getId()))
                .findFirst()
                .orElseThrow();
        assertThat(frequenciaAna.encontros()).isEqualTo(3);
        assertThat(frequenciaAna.presencas()).isEqualTo(1);
        assertThat(frequenciaAna.ausencias()).isEqualTo(1);
        assertThat(frequenciaAna.justificadas()).isEqualTo(1);
        assertThat(frequenciaAna.percentual()).isEqualByComparingTo(new BigDecimal("33.3"));

        FrequenciaParticipanteDto frequenciaBia = frequencias.stream()
                .filter(item -> item.participanteId().equals(bia.getId()))
                .findFirst()
                .orElseThrow();
        assertThat(frequenciaBia.percentual()).isEqualByComparingTo(new BigDecimal("0.0"));
        assertThat(frequenciaBia.justificadas()).isEqualTo(1);
    }

    @Test
    void deveFiltrarHistoricoPorPeriodo() {
        presencaService.registrar(ana.getId(), encontroUm.getId(), StatusPresenca.PRESENTE, null);
        presencaService.registrar(ana.getId(), encontroDois.getId(), StatusPresenca.AUSENTE, null);

        var historico = presencaService.listarHistorico(
                ana.getId(), null, atividade.getId(),
                LocalDate.of(2026, 8, 15), LocalDate.of(2026, 8, 20),
                null, null
        );

        assertThat(historico).singleElement()
                .extracting(p -> p.getEncontro().getId())
                .isEqualTo(encontroDois.getId());
    }

    @Test
    void deveValidarOrdemDoPeriodo() {
        assertThatThrownBy(() -> presencaService.calcularFrequencias(
                null, LocalDate.of(2026, 8, 20), LocalDate.of(2026, 8, 10), null
        ))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessage("A data inicial deve ser anterior ou igual à data final.");
    }

    private RegistroPresencaForm formulario(Encontro encontro, ItemRegistroPresencaForm... itens) {
        return new RegistroPresencaForm(encontro.getId(), List.of(itens));
    }

    private ItemRegistroPresencaForm item(Participante participante, StatusPresenca status) {
        return new ItemRegistroPresencaForm(
                participante.getId(), participante.getNome(), status, null
        );
    }
}
