package com.ibmec.mulheresvalentes.service;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;

import static org.assertj.core.api.Assertions.assertThat;

class ConferenciaInscricaoServiceTest {

    private final ConferenciaInscricaoService servico = new ConferenciaInscricaoService();

    @Test
    void codigoSoLiberaUmaVez() {
        MockHttpSession sessao = new MockHttpSession();
        String codigo = servico.emitir(sessao);

        assertThat(servico.consumir(sessao, codigo)).isEqualTo(ConferenciaInscricaoService.Resultado.LIBERADA);
        servico.marcarEnviada(sessao, codigo);
        assertThat(servico.consumir(sessao, codigo)).isEqualTo(ConferenciaInscricaoService.Resultado.JA_ENVIADA);
    }

    @Test
    void codigoDeOutraSessaoNaoServe() {
        String codigo = servico.emitir(new MockHttpSession());

        assertThat(servico.consumir(new MockHttpSession(), codigo))
                .isEqualTo(ConferenciaInscricaoService.Resultado.DESCONHECIDA);
    }

    @Test
    void codigoDevolvidoPodeSerUsadoDeNovo() {
        MockHttpSession sessao = new MockHttpSession();
        String codigo = servico.emitir(sessao);
        servico.consumir(sessao, codigo);

        servico.devolver(sessao, codigo);

        assertThat(servico.consumir(sessao, codigo)).isEqualTo(ConferenciaInscricaoService.Resultado.LIBERADA);
    }
}
