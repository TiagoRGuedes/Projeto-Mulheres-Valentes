package com.ibmec.mulheresvalentes.service;

import com.ibmec.mulheresvalentes.exception.RegraNegocioException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Transactional
class ParticipanteServiceIntegrationTest {

    @Autowired
    private ParticipanteService participanteService;

    @Test
    void deveCadastrarEditarDesativarEReativarSemExcluir() {
        var participante = participanteService.cadastrar(
                "  Pessoa Fictícia  ", "  (11) 90000-0000  ", "  Observação  ");

        assertThat(participante.getNome()).isEqualTo("Pessoa Fictícia");
        assertThat(participante.getContato()).isEqualTo("(11) 90000-0000");
        assertThat(participante.getObservacoes()).isEqualTo("Observação");
        assertThat(participante.isAtivo()).isTrue();

        participanteService.atualizar(participante.getId(), "Pessoa Editada", null, null);
        assertThat(participanteService.buscar(participante.getId()).getContato()).isNull();
        participanteService.desativar(participante.getId());
        assertThat(participanteService.buscar(participante.getId()).isAtivo()).isFalse();

        participanteService.ativar(participante.getId());
        assertThat(participanteService.buscar(participante.getId()).isAtivo()).isTrue();
    }

    @Test
    void deveEncontrarParticipantePeloContato() {
        participanteService.cadastrar("Pessoa Localizável", "(21) 98888-7777", null);

        assertThat(participanteService.listar("98888"))
                .extracting(com.ibmec.mulheresvalentes.model.Participante::getNome)
                .contains("Pessoa Localizável");
    }

    @Test
    void deveRejeitarContatoAcimaDoLimite() {
        assertThatThrownBy(() -> participanteService.cadastrar(
                "Pessoa Fictícia", "x".repeat(121), null))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessage("O contato deve ter no máximo 120 caracteres.");
    }

    @Test
    void deveRejeitarNomeEmBrancoMesmoForaDoController() {
        assertThatThrownBy(() -> participanteService.cadastrar("   ", null, null))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessage("Informe o nome do participante.");
    }
}
