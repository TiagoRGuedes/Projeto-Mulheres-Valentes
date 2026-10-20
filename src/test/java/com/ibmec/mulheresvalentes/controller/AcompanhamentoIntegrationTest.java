package com.ibmec.mulheresvalentes.controller;

import com.ibmec.mulheresvalentes.dto.ContatoInscricaoForm;
import com.ibmec.mulheresvalentes.dto.InscricaoForm;
import com.ibmec.mulheresvalentes.dto.LancamentoCaixaForm;
import com.ibmec.mulheresvalentes.exception.RegraNegocioException;
import com.ibmec.mulheresvalentes.model.CanalContato;
import com.ibmec.mulheresvalentes.model.CategoriaFinanceira;
import com.ibmec.mulheresvalentes.model.ContatoInscricao;
import com.ibmec.mulheresvalentes.model.Inscricao;
import com.ibmec.mulheresvalentes.model.LancamentoCaixa;
import com.ibmec.mulheresvalentes.model.PerfilUsuario;
import com.ibmec.mulheresvalentes.model.PreferenciaContato;
import com.ibmec.mulheresvalentes.model.ResultadoContato;
import com.ibmec.mulheresvalentes.model.StatusInscricao;
import com.ibmec.mulheresvalentes.model.TipoLancamento;
import com.ibmec.mulheresvalentes.model.TipoNotificacao;
import com.ibmec.mulheresvalentes.model.Usuario;
import com.ibmec.mulheresvalentes.repository.ComprovanteArquivoRepository;
import com.ibmec.mulheresvalentes.repository.HistoricoOperacaoRepository;
import com.ibmec.mulheresvalentes.repository.NotificacaoRepository;
import com.ibmec.mulheresvalentes.security.UsuarioAutenticado;
import com.ibmec.mulheresvalentes.service.CategoriaFinanceiraService;
import com.ibmec.mulheresvalentes.service.ComprovanteArquivoService;
import com.ibmec.mulheresvalentes.service.ContatoInscricaoService;
import com.ibmec.mulheresvalentes.service.InscricaoService;
import com.ibmec.mulheresvalentes.service.LancamentoCaixaService;
import com.ibmec.mulheresvalentes.service.NotificacaoService;
import com.ibmec.mulheresvalentes.service.UsuarioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Contatos das inscrições, notificações e foto do comprovante. */
@SpringBootTest
@Transactional
class AcompanhamentoIntegrationTest {

    private static final byte[] JPG = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0, 0, 0x10, 'J', 'F', 'I', 'F'};
    private static final byte[] PDF = "%PDF-1.4\n%fim".getBytes();

    @Autowired private WebApplicationContext contexto;
    @Autowired private UsuarioService usuarioService;
    @Autowired private InscricaoService inscricaoService;
    @Autowired private ContatoInscricaoService contatoService;
    @Autowired private NotificacaoService notificacaoService;
    @Autowired private NotificacaoRepository notificacaoRepository;
    @Autowired private HistoricoOperacaoRepository historicoRepository;
    @Autowired private CategoriaFinanceiraService categoriaService;
    @Autowired private LancamentoCaixaService lancamentoService;
    @Autowired private ComprovanteArquivoService comprovanteService;
    @Autowired private ComprovanteArquivoRepository comprovanteRepository;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private jakarta.persistence.EntityManager entityManager;

    private MockMvc mvc;
    private UsuarioAutenticado admin;
    private UsuarioAutenticado operadora;

    @BeforeEach
    void preparar() {
        mvc = MockMvcBuilders.webAppContextSetup(contexto).apply(springSecurity()).build();
        admin = new UsuarioAutenticado(criar("Admin Acompanhamento", "admin.acomp", PerfilUsuario.ADMIN));
        operadora = new UsuarioAutenticado(criar("Operadora Acompanhamento", "operadora.acomp", PerfilUsuario.OPERADOR));
    }

    // ---------- Notificações ----------

    @Test
    void novaInscricaoGeraNotificacaoQueApareceNoSinoAteSerVista() throws Exception {
        Inscricao inscricao = inscricaoService.receber(formInscricao("Joana Teste Silva", "(11) 98888-0001"));

        assertThat(notificacaoRepository.existsByTipoAndReferenciaId(TipoNotificacao.NOVA_INSCRICAO, inscricao.getId())).isTrue();
        long antes = notificacaoService.contarNaoVistas(operadora.getId());
        assertThat(antes).isPositive();

        mvc.perform(get("/painel").with(user(operadora)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("sino-contador")));
        mvc.perform(get("/notificacoes").with(user(operadora)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Joana, de Pirituba, se inscreveu pelo site")))
                .andExpect(content().string(not(containsString("98888-0001"))));

        assertThat(notificacaoService.contarNaoVistas(operadora.getId())).isZero();
        // Cada pessoa tem o próprio "visto": para a admin continua novo.
        assertThat(notificacaoService.contarNaoVistas(admin.getId())).isPositive();
    }

    @Test
    void avisaUmaVezSobreInscricaoSemContatoHaMaisDeTresDias() {
        Inscricao antiga = inscricaoService.receber(formInscricao("Ana Antiga", "(11) 98888-0002"));
        Inscricao recente = inscricaoService.receber(formInscricao("Bia Recente", "(11) 98888-0003"));
        entityManager.flush();
        jdbc.update("update inscricoes set recebida_em = ? where id = ?", LocalDateTime.now().minusDays(5), antiga.getId());
        entityManager.clear();

        int criados = notificacaoService.verificarInscricoesSemContato(3);

        assertThat(criados).isGreaterThanOrEqualTo(1);
        assertThat(notificacaoRepository.existsByTipoAndReferenciaId(TipoNotificacao.INSCRICAO_SEM_CONTATO, antiga.getId())).isTrue();
        assertThat(notificacaoRepository.existsByTipoAndReferenciaId(TipoNotificacao.INSCRICAO_SEM_CONTATO, recente.getId())).isFalse();
        assertThat(notificacaoService.verificarInscricoesSemContato(3)).isZero();
        assertThat(contatoService.esperandoPrimeiroContato(3)).extracting(Inscricao::getId).contains(antiga.getId());
    }

    // ---------- Contatos das inscrições ----------

    @Test
    void registrarContatoPelaTelaGuardaAutoraEPassaParaEmContato() throws Exception {
        Inscricao inscricao = inscricaoService.receber(formInscricao("Carla Contato", "(11) 98888-0004"));

        mvc.perform(get("/inscricoes/" + inscricao.getId()).with(user(operadora)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Registrar um contato")));

        mvc.perform(post("/inscricoes/" + inscricao.getId() + "/contatos").with(user(operadora)).with(csrf())
                        .param("realizadoEm", agora())
                        .param("canal", "WHATSAPP")
                        .param("resultado", "PEDIU_RETORNO")
                        .param("observacao", "Pediu para ligar depois das 18h, informação reservada"))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attribute("sucesso", containsString("Em contato")));

        assertThat(inscricaoService.buscar(inscricao.getId()).getStatus()).isEqualTo(StatusInscricao.EM_CONTATO);
        List<ContatoInscricao> contatos = contatoService.listar(inscricao.getId());
        assertThat(contatos).hasSize(1);
        assertThat(contatos.getFirst().getRegistradoPorNome()).isEqualTo("Operadora Acompanhamento");
        assertThat(contatos.getFirst().getResultado()).isEqualTo(ResultadoContato.PEDIU_RETORNO);
        assertThat(historicoRepository.findAll()).noneMatch(h -> h.getDescricao().contains("informação reservada"));

        mvc.perform(get("/inscricoes").with(user(operadora)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("1 contato(s)")));
    }

    @Test
    void contatoComFaltaDeDadosOuDataNoFuturoNaoEGravado() throws Exception {
        Inscricao inscricao = inscricaoService.receber(formInscricao("Dora Dados", "(11) 98888-0005"));

        mvc.perform(post("/inscricoes/" + inscricao.getId() + "/contatos").with(user(operadora)).with(csrf())
                        .param("realizadoEm", agora())
                        .param("canal", "LIGACAO"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Escolha como foi o contato")));

        ContatoInscricaoForm futuro = new ContatoInscricaoForm();
        futuro.setRealizadoEm(LocalDateTime.now().plusDays(1));
        futuro.setCanal(CanalContato.LIGACAO);
        futuro.setResultado(ResultadoContato.CONVERSOU);
        assertThatThrownBy(() -> contatoService.registrar(inscricao.getId(), futuro))
                .isInstanceOf(RegraNegocioException.class).hasMessageContaining("futuro");

        assertThat(contatoService.listar(inscricao.getId())).isEmpty();
        assertThat(inscricaoService.buscar(inscricao.getId()).getStatus()).isEqualTo(StatusInscricao.NOVA);
    }

    // ---------- Foto do comprovante ----------

    @Test
    void anexarAbrirSubstituirERemoverComprovante() throws Exception {
        LancamentoCaixa lancamento = lancamento();
        String base = "/caixa/movimentacoes/" + lancamento.getId() + "/comprovante";

        mvc.perform(multipart(base).file(new MockMultipartFile("arquivo", "recibo.jpg", "image/jpeg", JPG))
                        .with(user(operadora)).with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attribute("sucesso", containsString("guardada")));

        mvc.perform(get(base).with(user(operadora)))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "image/jpeg"))
                .andExpect(header().string("X-Content-Type-Options", "nosniff"));
        mvc.perform(get("/caixa/movimentacoes/" + lancamento.getId()).with(user(operadora)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("recibo.jpg")))
                .andExpect(content().string(not(containsString("/comprovante/remover"))));

        mvc.perform(multipart(base).file(new MockMultipartFile("arquivo", "nota.pdf", "application/pdf", PDF))
                        .with(user(operadora)).with(csrf()))
                .andExpect(status().is3xxRedirection());
        assertThat(comprovanteRepository.findByLancamentoId(lancamento.getId()).orElseThrow().getTipoConteudo())
                .isEqualTo("application/pdf");

        mvc.perform(post(base + "/remover").with(user(operadora)).with(csrf())).andExpect(status().isForbidden());
        mvc.perform(post(base + "/remover").with(user(admin)).with(csrf())).andExpect(status().is3xxRedirection());
        assertThat(comprovanteRepository.findByLancamentoId(lancamento.getId())).isEmpty();
    }

    @Test
    void recusaArquivoQueNaoEFotoNemPdfMesmoComExtensaoDeFoto() throws Exception {
        LancamentoCaixa lancamento = lancamento();
        String base = "/caixa/movimentacoes/" + lancamento.getId() + "/comprovante";

        mvc.perform(multipart(base).file(new MockMultipartFile("arquivo", "falso.jpg", "image/jpeg", "<html>oi</html>".getBytes()))
                        .with(user(operadora)).with(csrf()))
                .andExpect(flash().attribute("erro", containsString("Formato não aceito")));
        mvc.perform(multipart(base).with(user(operadora)).with(csrf()))
                .andExpect(flash().attribute("erro", containsString("Escolha uma foto")));
        byte[] grande = new byte[(int) ComprovanteArquivoService.TAMANHO_MAXIMO + 1];
        System.arraycopy(JPG, 0, grande, 0, JPG.length);
        mvc.perform(multipart(base).file(new MockMultipartFile("arquivo", "grande.jpg", "image/jpeg", grande))
                        .with(user(operadora)).with(csrf()))
                .andExpect(flash().attribute("erro", containsString("5 MB")));

        assertThat(comprovanteService.buscar(lancamento.getId())).isEmpty();
        mvc.perform(get(base).with(user(operadora))).andExpect(status().isNotFound());
    }

    @Test
    void comprovanteExigeLogin() throws Exception {
        LancamentoCaixa lancamento = lancamento();
        mvc.perform(get("/caixa/movimentacoes/" + lancamento.getId() + "/comprovante"))
                .andExpect(status().is3xxRedirection());
    }

    // ---------- apoio ----------

    private static String agora() {
        return java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm").format(LocalDateTime.now());
    }

    private Usuario criar(String nome, String login, PerfilUsuario perfil) {
        Usuario usuario = usuarioService.cadastrar(nome, login, perfil, "temporaria123");
        usuarioService.trocarPropriaSenha(usuario.getId(), "temporaria123", "senha-definitiva-1");
        return usuarioService.buscar(usuario.getId());
    }

    private static InscricaoForm formInscricao(String nome, String telefone) {
        InscricaoForm form = new InscricaoForm();
        form.setNome(nome);
        form.setContato(telefone);
        form.setPreferenciaContato(PreferenciaContato.WHATSAPP);
        form.setBairro("Pirituba");
        form.setAreas(Set.of());
        form.setConsentimento(true);
        return form;
    }

    private LancamentoCaixa lancamento() {
        CategoriaFinanceira categoria = categoriaService.cadastrar("Categoria comprovante " + System.nanoTime(), TipoLancamento.ENTRADA);
        LancamentoCaixaForm form = new LancamentoCaixaForm();
        form.setData(LocalDate.now());
        form.setCategoriaId(categoria.getId());
        form.setDescricao("Lançamento com comprovante");
        form.setValor(new BigDecimal("100.00"));
        return lancamentoService.registrarEntrada(form);
    }
}
