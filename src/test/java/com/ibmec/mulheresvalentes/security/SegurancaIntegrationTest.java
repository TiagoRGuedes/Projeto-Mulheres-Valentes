package com.ibmec.mulheresvalentes.security;

import com.ibmec.mulheresvalentes.model.PerfilUsuario;
import com.ibmec.mulheresvalentes.model.Usuario;
import com.ibmec.mulheresvalentes.repository.UsuarioRepository;
import com.ibmec.mulheresvalentes.service.UsuarioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest
@Transactional
class SegurancaIntegrationTest {

    @Autowired
    private WebApplicationContext contexto;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    private MockMvc mvc;
    private UsuarioAutenticado admin;
    private UsuarioAutenticado operador;

    @BeforeEach
    void preparar() {
        mvc = MockMvcBuilders.webAppContextSetup(contexto).apply(springSecurity()).build();
        admin = new UsuarioAutenticado(criarComSenhaPropria("Admin Teste", "admin.teste", PerfilUsuario.ADMIN));
        operador = new UsuarioAutenticado(criarComSenhaPropria("Operadora Teste", "operadora.teste", PerfilUsuario.OPERADOR));
    }

    @Test
    void paginaPublicaEInscricaoAbremSemLogin() throws Exception {
        mvc.perform(get("/")).andExpect(status().isOk());
        mvc.perform(get("/inscricao")).andExpect(status().isOk());
        mvc.perform(get("/login")).andExpect(status().isOk()).andExpect(view().name("auth/login"));
        mvc.perform(get("/css/app.css")).andExpect(status().isOk());
    }

    @Test
    void painelSemLoginLevaParaATelaDeLogin() throws Exception {
        for (String endereco : new String[] {"/painel", "/inscricoes", "/participantes", "/caixa", "/usuarios"}) {
            mvc.perform(get(endereco)).andExpect(redirectedUrlPattern("**/login"));
        }
        mvc.perform(get("/api/caixa/saldo")).andExpect(status().isUnauthorized());
    }

    @Test
    void formularioPublicoExigeTokenCsrf() throws Exception {
        mvc.perform(post("/inscricao").param("nome", "Teste")).andExpect(status().isForbidden());
        mvc.perform(post("/inscricao").with(csrf()).param("nome", "Teste")).andExpect(status().isOk());
    }

    @Test
    void operadorUsaODiaADiaMasNaoAreasDeAdministracao() throws Exception {
        for (String endereco : new String[] {"/painel", "/inscricoes", "/participantes", "/presencas/registrar", "/caixa", "/caixa/entrada", "/relatorios"}) {
            mvc.perform(get(endereco).with(user(operador))).andExpect(status().isOk());
        }
        for (String endereco : new String[] {"/usuarios", "/usuarios/novo", "/historico", "/caixa/categorias"}) {
            mvc.perform(get(endereco).with(user(operador))).andExpect(status().isForbidden());
        }
        mvc.perform(post("/usuarios").with(user(operador)).with(csrf())
                        .param("nome", "Invasora").param("login", "invasora").param("perfil", "ADMIN")
                        .param("senha", "temporaria123").param("confirmacaoSenha", "temporaria123"))
                .andExpect(status().isForbidden());
        assertThat(usuarioRepository.findByLogin("invasora")).isEmpty();
    }

    @Test
    void adminCadastraUsuarioPelaTela() throws Exception {
        mvc.perform(get("/usuarios").with(user(admin))).andExpect(status().isOk());
        mvc.perform(get("/historico").with(user(admin))).andExpect(status().isOk());

        mvc.perform(post("/usuarios").with(user(admin)).with(csrf())
                        .param("nome", "Isabelle Teste").param("login", "isabelle").param("perfil", "OPERADOR")
                        .param("senha", "temporaria123").param("confirmacaoSenha", "temporaria123"))
                .andExpect(status().is3xxRedirection());

        Usuario criado = usuarioRepository.findByLogin("isabelle").orElseThrow();
        assertThat(criado.getPerfil()).isEqualTo(PerfilUsuario.OPERADOR);
        assertThat(criado.isDeveTrocarSenha()).isTrue();
    }

    @Test
    void loginComSenhaCertaEntraEComSenhaErradaNao() throws Exception {
        mvc.perform(formLogin("/login").user("login", "admin.teste").password("senha", "errada-errada"))
                .andExpect(unauthenticated())
                .andExpect(redirectedUrl("/login?erro"));
        mvc.perform(formLogin("/login").user("login", "admin.teste").password("senha", "senha-definitiva-1"))
                .andExpect(authenticated().withRoles("ADMIN"))
                .andExpect(redirectedUrl("/painel"));
    }

    @Test
    void senhaTemporariaObrigaATrocarAntesDeUsarOPainel() throws Exception {
        usuarioService.cadastrar("Nova Pessoa", "nova.pessoa", PerfilUsuario.OPERADOR, "temporaria123");
        MockHttpSession sessao = (MockHttpSession) mvc.perform(
                        formLogin("/login").user("login", "nova.pessoa").password("senha", "temporaria123"))
                .andExpect(authenticated())
                .andReturn().getRequest().getSession();

        mvc.perform(get("/painel").session(sessao)).andExpect(redirectedUrl("/minha-conta/senha"));
        mvc.perform(get("/minha-conta/senha").session(sessao)).andExpect(status().isOk());

        mvc.perform(post("/minha-conta/senha").session(sessao).with(csrf())
                        .param("senhaAtual", "temporaria123")
                        .param("novaSenha", "minha-senha-nova").param("confirmacaoSenha", "minha-senha-nova"))
                .andExpect(redirectedUrl("/painel"));
        mvc.perform(get("/painel").session(sessao)).andExpect(status().isOk());
    }

    @Test
    void usuarioDesativadoNaoEntra() throws Exception {
        Usuario usuario = usuarioRepository.findByLogin("operadora.teste").orElseThrow();
        usuario.desativar();
        usuarioRepository.save(usuario);

        mvc.perform(formLogin("/login").user("login", "operadora.teste").password("senha", "senha-definitiva-1"))
                .andExpect(unauthenticated());
        mvc.perform(get("/painel").with(user(operador))).andExpect(redirectedUrl("/login?desativado"));
    }

    @Test
    void sairEncerraASessao() throws Exception {
        mvc.perform(post("/sair").with(user(admin)).with(csrf()))
                .andExpect(redirectedUrl("/login?saiu"));
    }

    private Usuario criarComSenhaPropria(String nome, String login, PerfilUsuario perfil) {
        Usuario usuario = usuarioService.cadastrar(nome, login, perfil, "temporaria123");
        usuarioService.trocarPropriaSenha(usuario.getId(), "temporaria123", "senha-definitiva-1");
        return usuarioService.buscar(usuario.getId());
    }
}
