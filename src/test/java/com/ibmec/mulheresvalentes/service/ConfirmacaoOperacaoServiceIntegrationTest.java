package com.ibmec.mulheresvalentes.service;

import com.ibmec.mulheresvalentes.exception.RegraNegocioException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class ConfirmacaoOperacaoServiceIntegrationTest {

    @Autowired
    private ConfirmacaoOperacaoService service;

    @Test
    void deveRegistrarConsumirPayloadESerDisponivelComoBeanSpring() {
        MockHttpSession sessao = new MockHttpSession();
        DadosRevisao dados = new DadosRevisao("Participante de teste");
        String token = service.gerarToken(sessao, "CRIAR_PARTICIPANTE", dados);

        assertThat(service.obterPayloadSemConsumir(
                token, sessao, "CRIAR_PARTICIPANTE", DadosRevisao.class))
                .isEqualTo(dados);

        DadosRevisao consumidos = service.consumirToken(
                token, sessao, "CRIAR_PARTICIPANTE", DadosRevisao.class);
        assertThat(consumidos).isEqualTo(dados);
        assertThatThrownBy(() -> service.consumirToken(
                token, sessao, "CRIAR_PARTICIPANTE", DadosRevisao.class))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("não é válida");
    }

    private record DadosRevisao(String nome) {
    }
}
