package com.ibmec.mulheresvalentes.controller;

import com.ibmec.mulheresvalentes.dto.ResumoFinanceiroDTO;
import com.ibmec.mulheresvalentes.security.SessaoAdministrador;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class PainelRedesenhadoTest {

    @Autowired
    private WebApplicationContext contexto;

    private MockMvc mvc;
    private MockHttpSession sessao;

    @BeforeEach
    void preparar() {
        mvc = MockMvcBuilders.webAppContextSetup(contexto).build();
        sessao = new MockHttpSession();
        sessao.setAttribute(SessaoAdministrador.CHAVE_SESSAO, Boolean.TRUE);
    }

    @Test
    void visaoGeralMostraAsAcoesDoDiaEOResumoDaOng() throws Exception {
        mvc.perform(get("/painel").session(sessao))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("O que você precisa fazer hoje?")))
                .andExpect(content().string(containsString("Como está a ONG")))
                .andExpect(content().string(containsString("Mais opções")));
    }

    @Test
    void filtroDaTabelaDoCaixaNaoAlteraOsIndicadores() throws Exception {
        ResumoFinanceiroDTO semFiltro = (ResumoFinanceiroDTO) mvc.perform(get("/caixa").session(sessao))
                .andExpect(status().isOk())
                .andReturn().getModelAndView().getModel().get("resumo");
        ResumoFinanceiroDTO comFiltro = (ResumoFinanceiroDTO) mvc.perform(get("/caixa").session(sessao).param("tipo", "SAIDA"))
                .andExpect(status().isOk())
                .andReturn().getModelAndView().getModel().get("resumo");

        assertThat(comFiltro).isEqualTo(semFiltro);
    }

    @Test
    void filtroDeDoacoesMarcaAPilulaCerta() throws Exception {
        mvc.perform(get("/caixa").session(sessao).param("natureza", "DOACAO_MATERIAL"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("aria-current=\"true\" class=\"ativo\">Doações</a>")));
    }
}
