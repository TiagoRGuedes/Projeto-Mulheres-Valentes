package com.ibmec.mulheresvalentes.controller;

import com.ibmec.mulheresvalentes.repository.InscricaoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest
class InscricaoPublicaFluxoTest {

    @Autowired
    private WebApplicationContext contexto;

    @Autowired
    private InscricaoRepository inscricaoRepository;

    private MockMvc mvc;

    @BeforeEach
    void preparar() {
        mvc = MockMvcBuilders.webAppContextSetup(contexto).build();
    }

    @Test
    void conferirNaoGravaNadaEMostraOContatoFormatado() throws Exception {
        long antes = inscricaoRepository.count();

        mvc.perform(preenchida(post("/inscricao"), "11912345678"))
                .andExpect(status().isOk())
                .andExpect(view().name("publico/inscricao-conferir"))
                .andExpect(content().string(containsString("(11) 91234-5678")));

        assertThat(inscricaoRepository.count()).isEqualTo(antes);
    }

    @Test
    void contatoSemDddVoltaParaOFormularioComOAviso() throws Exception {
        mvc.perform(preenchida(post("/inscricao"), "91234-5678"))
                .andExpect(status().isOk())
                .andExpect(view().name("publico/inscricao"))
                .andExpect(content().string(containsString("Parece faltar o DDD")));
    }

    @Test
    void confirmarGravaUmaVezMesmoComCliqueDuplo() throws Exception {
        MockHttpSession sessao = new MockHttpSession();
        String codigo = codigoDaConferencia(mvc.perform(
                preenchida(post("/inscricao"), "(11) 95555-0001").session(sessao)).andReturn());
        long antes = inscricaoRepository.count();

        mvc.perform(preenchida(post("/inscricao/confirmar"), "(11) 95555-0001")
                        .session(sessao).param("conferencia", codigo))
                .andExpect(redirectedUrl("/inscricao/recebida"));
        mvc.perform(preenchida(post("/inscricao/confirmar"), "(11) 95555-0001")
                        .session(sessao).param("conferencia", codigo))
                .andExpect(redirectedUrl("/inscricao/recebida"));

        assertThat(inscricaoRepository.count()).isEqualTo(antes + 1);
    }

    @Test
    void conferenciaPerdidaPedeConfirmarDeNovoSemPerderOsDados() throws Exception {
        long antes = inscricaoRepository.count();

        mvc.perform(preenchida(post("/inscricao/confirmar"), "(11) 95555-0002")
                        .session(new MockHttpSession()).param("conferencia", "codigo-que-nao-existe"))
                .andExpect(status().isOk())
                .andExpect(view().name("publico/inscricao-conferir"))
                .andExpect(content().string(containsString("Confira os dados e confirme de novo")))
                .andExpect(content().string(containsString("Conferência Fictícia")));

        assertThat(inscricaoRepository.count()).isEqualTo(antes);
    }

    @Test
    void corrigirDevolveOFormularioPreenchido() throws Exception {
        mvc.perform(preenchida(post("/inscricao/corrigir"), "(11) 95555-0003"))
                .andExpect(view().name("publico/inscricao"))
                .andExpect(content().string(containsString("value=\"(11) 95555-0003\"")));
    }

    private MockHttpServletRequestBuilder preenchida(MockHttpServletRequestBuilder requisicao, String contato) {
        return requisicao
                .param("nome", "Conferência Fictícia")
                .param("contato", contato)
                .param("preferenciaContato", "WHATSAPP")
                .param("areas", "ACOLHIMENTO")
                .param("consentimento", "true");
    }

    private String codigoDaConferencia(MvcResult resultado) throws Exception {
        Matcher m = Pattern.compile("name=\"conferencia\" value=\"([^\"]+)\"")
                .matcher(resultado.getResponse().getContentAsString());
        assertThat(m.find()).isTrue();
        return m.group(1);
    }
}
