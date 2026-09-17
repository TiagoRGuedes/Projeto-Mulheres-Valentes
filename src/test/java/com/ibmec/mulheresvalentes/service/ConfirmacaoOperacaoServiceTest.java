package com.ibmec.mulheresvalentes.service;

import com.ibmec.mulheresvalentes.exception.RegraNegocioException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ConfirmacaoOperacaoServiceTest {

    private static final String SESSAO = "sessao-de-teste";
    private static final String OPERACAO = "EXCLUIR_LANCAMENTO:42";

    private RelogioMutavel relogio;
    private ConfirmacaoOperacaoService service;

    @BeforeEach
    void configurar() {
        relogio = new RelogioMutavel(
                Instant.parse("2026-08-31T15:00:00Z"), ZoneOffset.UTC);
        service = new ConfirmacaoOperacaoService(
                Duration.ofMinutes(10), relogio, new SecureRandom());
    }

    @Test
    void deveGerarTokenVinculadoASessaoEOperacao() {
        String token = service.gerarToken(SESSAO, OPERACAO);

        assertThat(token).isNotBlank().doesNotContain("=", "+", "/");
        assertThatCode(() -> service.validarToken(token, SESSAO, OPERACAO))
                .doesNotThrowAnyException();
        assertThatThrownBy(() -> service.validarToken(token, "outra-sessao", OPERACAO))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("não é válida");
        assertThatThrownBy(() -> service.validarToken(token, SESSAO, "OUTRA_OPERACAO"))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("não é válida");

        assertThatCode(() -> service.consumirToken(token, SESSAO, OPERACAO))
                .doesNotThrowAnyException();
    }

    @Test
    void validarNaoDeveConsumirToken() {
        String token = service.gerarToken(SESSAO, OPERACAO);

        service.validarToken(token, SESSAO, OPERACAO);
        service.validarToken(token, SESSAO, OPERACAO);

        assertThatCode(() -> service.consumirToken(token, SESSAO, OPERACAO))
                .doesNotThrowAnyException();
    }

    @Test
    void deveConsumirTokenUmaUnicaVezEImpedirSegundoPost() {
        String token = service.gerarToken(SESSAO, OPERACAO);

        service.consumirToken(token, SESSAO, OPERACAO);

        assertThatThrownBy(() -> service.consumirToken(token, SESSAO, OPERACAO))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("não é válida");
        assertThatThrownBy(() -> service.validarToken(token, SESSAO, OPERACAO))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("não é válida");
    }

    @Test
    void apenasUmDeDoisPostsConcorrentesDeveConsumirToken() throws Exception {
        String token = service.gerarToken(SESSAO, OPERACAO);
        CountDownLatch preparados = new CountDownLatch(2);
        CountDownLatch iniciar = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(2);

        try {
            var tentativa = (java.util.concurrent.Callable<Boolean>) () -> {
                preparados.countDown();
                iniciar.await();
                try {
                    service.consumirToken(token, SESSAO, OPERACAO);
                    return true;
                } catch (RegraNegocioException excecao) {
                    return false;
                }
            };
            List<Future<Boolean>> resultados = List.of(
                    executor.submit(tentativa),
                    executor.submit(tentativa));
            preparados.await();
            iniciar.countDown();

            assertThat(resultados)
                    .extracting(Future::get)
                    .containsExactlyInAnyOrder(true, false);
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    void deveRejeitarTokenInexistenteOuVazio() {
        assertThatThrownBy(() -> service.validarToken("token-inexistente", SESSAO, OPERACAO))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("não é válida");
        assertThatThrownBy(() -> service.consumirToken("  ", SESSAO, OPERACAO))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("não é válida");
    }

    @Test
    void deveRejeitarERemoverTokenExpirado() {
        String token = service.gerarToken(SESSAO, OPERACAO);
        relogio.avancar(Duration.ofMinutes(10));

        assertThatThrownBy(() -> service.validarToken(token, SESSAO, OPERACAO))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("expirou");
        assertThatThrownBy(() -> service.consumirToken(token, SESSAO, OPERACAO))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("não é válida");
    }

    private static final class RelogioMutavel extends Clock {

        private Instant instante;
        private final ZoneId zona;

        private RelogioMutavel(Instant instante, ZoneId zona) {
            this.instante = instante;
            this.zona = zona;
        }

        private void avancar(Duration duracao) {
            instante = instante.plus(duracao);
        }

        @Override
        public ZoneId getZone() {
            return zona;
        }

        @Override
        public Clock withZone(ZoneId novaZona) {
            return new RelogioMutavel(instante, novaZona);
        }

        @Override
        public Instant instant() {
            return instante;
        }
    }
}
