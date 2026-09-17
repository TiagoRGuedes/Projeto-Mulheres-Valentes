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
        var participante = participanteService.cadastrar("  Pessoa Fictícia  ", "  Observação  ");

        assertThat(participante.getNome()).isEqualTo("Pessoa Fictícia");
        assertThat(participante.getObservacoes()).isEqualTo("Observação");
        assertThat(participante.isAtivo()).isTrue();

        participanteService.atualizar(participante.getId(), "Pessoa Editada", null);
        participanteService.desativar(participante.getId());
        assertThat(participanteService.buscar(participante.getId()).isAtivo()).isFalse();

        participanteService.ativar(participante.getId());
        assertThat(participanteService.buscar(participante.getId()).isAtivo()).isTrue();
    }

    @Test
    void deveRejeitarNomeEmBrancoMesmoForaDoController() {
        assertThatThrownBy(() -> participanteService.cadastrar("   ", null))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessage("Informe o nome do participante.");
    }
}
