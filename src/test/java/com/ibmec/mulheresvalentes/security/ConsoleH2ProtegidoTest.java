package com.ibmec.mulheresvalentes.security;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.TestPropertySource;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * O console do H2 roda fora do Spring MVC: se ele ficar aberto, qualquer pessoa
 * lê e apaga o banco inteiro sem passar pelo login. Este teste existe para que
 * essa porta não seja reaberta sem querer.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {
        "spring.h2.console.enabled=true",
        "spring.h2.console.path=/h2-console"
})
class ConsoleH2ProtegidoTest {

    @LocalServerPort
    private int porta;

    @Test
    void consoleDoBancoNaoAbreSemLogin() throws Exception {
        HttpResponse<String> resposta = chamar("/h2-console/");

        assertThat(resposta.statusCode())
                .as("o console do H2 precisa exigir login")
                .isEqualTo(302);
        assertThat(resposta.headers().firstValue("Location").orElseThrow())
                .endsWith("/admin");
    }

    @Test
    void paginaPublicaContinuaAbertaComOFiltroAtivo() throws Exception {
        assertThat(chamar("/").statusCode())
                .as("o filtro do console não pode fechar o site da ONG")
                .isEqualTo(200);
    }

    private HttpResponse<String> chamar(String caminho) throws Exception {
        HttpClient cliente = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NEVER)
                .build();
        HttpRequest requisicao = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + porta + caminho))
                .GET()
                .build();
        return cliente.send(requisicao, HttpResponse.BodyHandlers.ofString());
    }
}
