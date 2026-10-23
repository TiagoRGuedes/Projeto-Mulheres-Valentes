package com.ibmec.mulheresvalentes.controller;

import com.ibmec.mulheresvalentes.security.SessaoAdministrador;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class AcessoAdministrativoTest {

    @Autowired
    private WebApplicationContext contexto;

    private MockMvc mvc;

    @BeforeEach
    void preparar() {
        mvc = MockMvcBuilders.webAppContextSetup(contexto).build();
    }

    @Test
    void painelSemLoginVoltaParaATelaDeEntrada() throws Exception {
        mvc.perform(get("/painel"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin"));
    }

    @Test
    void apiSemLoginRespondeNaoAutorizado() throws Exception {
        mvc.perform(get("/api/caixa/saldo"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void paginaPublicaEInscricaoContinuamAbertas() throws Exception {
        mvc.perform(get("/")).andExpect(status().isOk());
        mvc.perform(get("/inscricao")).andExpect(status().isOk());
    }

    @Test
    void rodapePublicoNaoLevaMaisAoPainel() throws Exception {
        mvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString("Área da equipe"))));
    }

    @Test
    void credenciaisCertasAbremOPainel() throws Exception {
        mvc.perform(post("/admin").param("usuario", "admin123").param("senha", "123456"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/painel"))
                .andExpect(request().sessionAttribute(SessaoAdministrador.CHAVE_SESSAO, true));
    }

    @Test
    void senhaErradaNaoCriaSessao() throws Exception {
        mvc.perform(post("/admin").param("usuario", "admin123").param("senha", "errada"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Usuário ou senha não conferem")))
                .andExpect(request().sessionAttributeDoesNotExist(SessaoAdministrador.CHAVE_SESSAO));
    }

    @Test
    void sairEncerraASessao() throws Exception {
        mvc.perform(post("/admin/sair"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin?saiu"));
    }
}
