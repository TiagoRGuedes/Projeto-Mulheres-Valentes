package com.ibmec.mulheresvalentes.service;

import com.ibmec.mulheresvalentes.exception.RegraNegocioException;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Mantém confirmações curtas no servidor para proteger operações sensíveis contra
 * reenvio acidental. O token só é aceito para a mesma sessão e a mesma operação
 * informadas no momento da geração.
 */
@Service
public class ConfirmacaoOperacaoService {

    static final Duration VALIDADE_PADRAO = Duration.ofMinutes(10);

    private static final int TAMANHO_TOKEN_EM_BYTES = 32;
    private static final String MENSAGEM_INVALIDA =
            "A confirmação não é válida para esta operação. "
                    + "Volte à tela anterior e tente novamente.";
    private static final String MENSAGEM_EXPIRADA =
            "Esta confirmação expirou. "
                    + "Volte à tela anterior e confirme a operação novamente.";

    private final Duration validade;
    private final Clock relogio;
    private final SecureRandom geradorAleatorio;
    private final ConcurrentMap<String, ConfirmacaoPendente> confirmacoes =
            new ConcurrentHashMap<>();

    public ConfirmacaoOperacaoService() {
        this(VALIDADE_PADRAO, Clock.systemUTC(), new SecureRandom());
    }

    ConfirmacaoOperacaoService(Duration validade, Clock relogio, SecureRandom geradorAleatorio) {
        if (validade == null || validade.isZero() || validade.isNegative()) {
            throw new IllegalArgumentException("A validade da confirmação deve ser positiva.");
        }
        this.validade = validade;
        this.relogio = Objects.requireNonNull(relogio, "O relógio é obrigatório.");
        this.geradorAleatorio = Objects.requireNonNull(
                geradorAleatorio, "O gerador aleatório é obrigatório.");
    }

    /**
     * Gera uma confirmação de uso único vinculada à sessão e à operação.
     *
     * @param sessaoId identificador da sessão HTTP atual
     * @param operacao chave estável da operação, por exemplo {@code EXCLUIR_LANCAMENTO:15}
     * @return token opaco que pode ser enviado como campo oculto do formulário
     */
    public String gerarToken(String sessaoId, String operacao) {
        return gerarToken(sessaoId, operacao, null);
    }

    public String gerarToken(HttpSession sessao, String operacao) {
        return gerarToken(exigirSessao(sessao).getId(), operacao, null);
    }

    /**
     * Gera uma confirmação que também preserva, somente no servidor, os dados
     * apresentados na revisão. Prefira DTOs imutáveis como payload.
     */
    public <T> String gerarToken(HttpSession sessao, String operacao, T payload) {
        return gerarToken(exigirSessao(sessao).getId(), operacao, payload);
    }

    private String gerarToken(String sessaoId, String operacao, Object payload) {
        String sessaoNormalizada = exigirIdentificador(sessaoId);
        String operacaoNormalizada = exigirOperacao(operacao);
        Instant agora = relogio.instant();
        removerExpiradas(agora);

        ConfirmacaoPendente confirmacao = new ConfirmacaoPendente(
                sessaoNormalizada,
                operacaoNormalizada,
                agora.plus(validade),
                payload);

        while (true) {
            String token = novoToken();
            if (confirmacoes.putIfAbsent(token, confirmacao) == null) {
                return token;
            }
        }
    }

    /**
     * Verifica a confirmação sem consumi-la. Pode ser usada para montar uma tela
     * intermediária; a operação definitiva ainda deve chamar {@link #consumirToken}.
     */
    public void validarToken(String token, String sessaoId, String operacao) {
        String tokenNormalizado = normalizarToken(token);
        obterConfirmacaoValida(tokenNormalizado, sessaoId, operacao);
    }

    public void validarToken(String token, HttpSession sessao, String operacao) {
        validarToken(token, exigirSessao(sessao).getId(), operacao);
    }

    /**
     * Valida sem consumir e devolve o payload guardado no servidor.
     */
    public <T> T obterPayloadSemConsumir(
            String token,
            HttpSession sessao,
            String operacao,
            Class<T> tipoPayload) {
        String tokenNormalizado = normalizarToken(token);
        ConfirmacaoPendente confirmacao = obterConfirmacaoValida(
                tokenNormalizado, exigirSessao(sessao).getId(), operacao);
        return converterPayload(confirmacao, tipoPayload);
    }

    /**
     * Valida e consome a confirmação de modo atômico. Somente uma chamada pode
     * concluir com sucesso, inclusive quando há dois POSTs concorrentes.
     */
    public void consumirToken(String token, String sessaoId, String operacao) {
        String tokenNormalizado = normalizarToken(token);
        consumirConfirmacao(tokenNormalizado, sessaoId, operacao, null);
    }

    public void consumirToken(String token, HttpSession sessao, String operacao) {
        consumirToken(token, exigirSessao(sessao).getId(), operacao);
    }

    /**
     * Consome a confirmação e devolve seu payload. A checagem do tipo acontece
     * antes do consumo, evitando perder um token por erro de integração.
     */
    public <T> T consumirToken(
            String token,
            HttpSession sessao,
            String operacao,
            Class<T> tipoPayload) {
        String tokenNormalizado = normalizarToken(token);
        ConfirmacaoPendente consumida = consumirConfirmacao(
                tokenNormalizado, exigirSessao(sessao).getId(), operacao, tipoPayload);
        return tipoPayload.cast(consumida.payload());
    }

    private ConfirmacaoPendente consumirConfirmacao(
            String tokenNormalizado,
            String sessaoId,
            String operacao,
            Class<?> tipoPayload) {
        while (true) {
            ConfirmacaoPendente confirmacao = confirmacoes.get(tokenNormalizado);
            if (confirmacao == null) {
                rejeitarInvalida();
            }

            validarVinculo(confirmacao, sessaoId, operacao);
            Instant agora = relogio.instant();
            if (confirmacao.expiradaEm(agora)) {
                if (confirmacoes.remove(tokenNormalizado, confirmacao)) {
                    throw new RegraNegocioException(MENSAGEM_EXPIRADA);
                }
                continue;
            }

            if (tipoPayload != null) {
                validarTipoPayload(confirmacao, tipoPayload);
            }
            if (confirmacoes.remove(tokenNormalizado, confirmacao)) {
                return confirmacao;
            }
        }
    }

    private ConfirmacaoPendente obterConfirmacaoValida(
            String tokenNormalizado,
            String sessaoId,
            String operacao) {
        ConfirmacaoPendente confirmacao = confirmacoes.get(tokenNormalizado);
        if (confirmacao == null) {
            rejeitarInvalida();
        }

        validarVinculo(confirmacao, sessaoId, operacao);
        Instant agora = relogio.instant();
        if (confirmacao.expiradaEm(agora)) {
            confirmacoes.remove(tokenNormalizado, confirmacao);
            throw new RegraNegocioException(MENSAGEM_EXPIRADA);
        }
        return confirmacao;
    }

    private <T> T converterPayload(ConfirmacaoPendente confirmacao, Class<T> tipoPayload) {
        validarTipoPayload(confirmacao, tipoPayload);
        return tipoPayload.cast(confirmacao.payload());
    }

    private void validarTipoPayload(ConfirmacaoPendente confirmacao, Class<?> tipoPayload) {
        Objects.requireNonNull(tipoPayload, "O tipo do payload é obrigatório.");
        if (!tipoPayload.isInstance(confirmacao.payload())) {
            rejeitarInvalida();
        }
    }

    private void validarVinculo(
            ConfirmacaoPendente confirmacao,
            String sessaoId,
            String operacao) {
        String sessaoNormalizada = normalizar(sessaoId);
        String operacaoNormalizada = normalizar(operacao);
        if (!confirmacao.sessaoId().equals(sessaoNormalizada)
                || !confirmacao.operacao().equals(operacaoNormalizada)) {
            rejeitarInvalida();
        }
    }

    private String novoToken() {
        byte[] bytes = new byte[TAMANHO_TOKEN_EM_BYTES];
        geradorAleatorio.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private void removerExpiradas(Instant agora) {
        for (Map.Entry<String, ConfirmacaoPendente> entrada : confirmacoes.entrySet()) {
            if (entrada.getValue().expiradaEm(agora)) {
                confirmacoes.remove(entrada.getKey(), entrada.getValue());
            }
        }
    }

    private String exigirIdentificador(String sessaoId) {
        String normalizado = normalizar(sessaoId);
        if (normalizado == null) {
            throw new RegraNegocioException(
                    "Não foi possível identificar a sessão para confirmar a operação.");
        }
        return normalizado;
    }

    private HttpSession exigirSessao(HttpSession sessao) {
        if (sessao == null) {
            throw new RegraNegocioException(
                    "Não foi possível identificar a sessão para confirmar a operação.");
        }
        return sessao;
    }

    private String exigirOperacao(String operacao) {
        String normalizado = normalizar(operacao);
        if (normalizado == null) {
            throw new RegraNegocioException("Informe qual operação precisa ser confirmada.");
        }
        return normalizado;
    }

    private String normalizarToken(String token) {
        String normalizado = normalizar(token);
        if (normalizado == null) {
            rejeitarInvalida();
        }
        return normalizado;
    }

    private String normalizar(String valor) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        return valor.trim();
    }

    private void rejeitarInvalida() {
        throw new RegraNegocioException(MENSAGEM_INVALIDA);
    }

    private record ConfirmacaoPendente(
            String sessaoId,
            String operacao,
            Instant expiraEm,
            Object payload) {

        private boolean expiradaEm(Instant instante) {
            return !expiraEm.isAfter(instante);
        }
    }
}
