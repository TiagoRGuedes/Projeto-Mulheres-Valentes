package com.ibmec.mulheresvalentes.service;

import com.ibmec.mulheresvalentes.exception.RegraNegocioException;
import com.ibmec.mulheresvalentes.model.HistoricoOperacao;
import com.ibmec.mulheresvalentes.model.PerfilUsuario;
import com.ibmec.mulheresvalentes.model.Usuario;
import com.ibmec.mulheresvalentes.repository.HistoricoOperacaoRepository;
import com.ibmec.mulheresvalentes.repository.UsuarioRepository;
import com.ibmec.mulheresvalentes.security.UsuarioAutenticado;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Transactional
class UsuarioServiceIntegrationTest {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private HistoricoOperacaoRepository historicoRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @AfterEach
    void limparLogin() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void cadastraComLoginNormalizadoESenhaCriptografada() {
        Usuario usuario = usuarioService.cadastrar("  Karine  Teste ", "  Karine.T ", PerfilUsuario.OPERADOR, "temporaria123");

        assertThat(usuario.getNome()).isEqualTo("Karine Teste");
        assertThat(usuario.getLogin()).isEqualTo("karine.t");
        assertThat(usuario.getSenhaHash()).isNotEqualTo("temporaria123").startsWith("{bcrypt}");
        assertThat(passwordEncoder.matches("temporaria123", usuario.getSenhaHash())).isTrue();
        assertThat(usuario.isDeveTrocarSenha()).isTrue();
        assertThat(usuario.isAtivo()).isTrue();
    }

    @Test
    void naoAceitaLoginRepetidoSenhaCurtaOuLoginInvalido() {
        usuarioService.cadastrar("Pessoa Um", "pessoa", PerfilUsuario.OPERADOR, "temporaria123");

        assertThatThrownBy(() -> usuarioService.cadastrar("Pessoa Dois", "PESSOA", PerfilUsuario.OPERADOR, "temporaria123"))
                .isInstanceOf(RegraNegocioException.class).hasMessageContaining("Já existe");
        assertThatThrownBy(() -> usuarioService.cadastrar("Pessoa Três", "pessoa3", PerfilUsuario.OPERADOR, "curta"))
                .isInstanceOf(RegraNegocioException.class).hasMessageContaining("pelo menos 8");
        assertThatThrownBy(() -> usuarioService.cadastrar("Pessoa Quatro", "joão silva", PerfilUsuario.OPERADOR, "temporaria123"))
                .isInstanceOf(RegraNegocioException.class).hasMessageContaining("login");
    }

    @Test
    void trocaDeSenhaExigeASenhaAtualELiberaOAcesso() {
        Usuario usuario = usuarioService.cadastrar("Pessoa", "pessoa.senha", PerfilUsuario.OPERADOR, "temporaria123");

        assertThatThrownBy(() -> usuarioService.trocarPropriaSenha(usuario.getId(), "errada", "minha-senha-nova"))
                .isInstanceOf(RegraNegocioException.class).hasMessageContaining("não confere");
        assertThatThrownBy(() -> usuarioService.trocarPropriaSenha(usuario.getId(), "temporaria123", "temporaria123"))
                .isInstanceOf(RegraNegocioException.class).hasMessageContaining("diferente");

        usuarioService.trocarPropriaSenha(usuario.getId(), "temporaria123", "minha-senha-nova");
        Usuario atualizado = usuarioService.buscar(usuario.getId());
        assertThat(atualizado.isDeveTrocarSenha()).isFalse();
        assertThat(passwordEncoder.matches("minha-senha-nova", atualizado.getSenhaHash())).isTrue();

        usuarioService.redefinirSenha(usuario.getId(), "outra-temporaria");
        assertThat(usuarioService.buscar(usuario.getId()).isDeveTrocarSenha()).isTrue();
    }

    @Test
    void sempreSobraUmaPessoaAdministradoraAtiva() {
        usuarioRepository.findAll().stream().filter(Usuario::isAdmin).forEach(u -> {
            u.desativar();
            usuarioRepository.save(u);
        });
        Usuario ana = usuarioService.cadastrar("Ana", "ana", PerfilUsuario.ADMIN, "temporaria123");
        Usuario bia = usuarioService.cadastrar("Bia", "bia", PerfilUsuario.ADMIN, "temporaria123");

        assertThatThrownBy(() -> usuarioService.desativar(ana.getId(), ana.getId()))
                .isInstanceOf(RegraNegocioException.class).hasMessageContaining("própria conta");

        usuarioService.desativar(bia.getId(), ana.getId());

        assertThatThrownBy(() -> usuarioService.atualizar(ana.getId(), "Ana", PerfilUsuario.OPERADOR, bia.getId()))
                .isInstanceOf(RegraNegocioException.class).hasMessageContaining("pelo menos uma");
        assertThatThrownBy(() -> usuarioService.atualizar(ana.getId(), "Ana", PerfilUsuario.OPERADOR, ana.getId()))
                .isInstanceOf(RegraNegocioException.class).hasMessageContaining("próprio perfil");
    }

    @Test
    void historicoGuardaQuemFezSemGuardarSenha() {
        Usuario admin = usuarioService.cadastrar("Admin Autora", "admin.autora", PerfilUsuario.ADMIN, "temporaria123");
        UsuarioAutenticado logado = new UsuarioAutenticado(admin);
        SecurityContextHolder.getContext().setAuthentication(
                UsernamePasswordAuthenticationToken.authenticated(logado, null, logado.getAuthorities()));

        Usuario novo = usuarioService.cadastrar("Operadora Nova", "operadora.nova", PerfilUsuario.OPERADOR, "senha-secreta-1");

        HistoricoOperacao registro = historicoRepository.findAll().stream()
                .filter(h -> "Usuario".equals(h.getTipoEntidade()) && novo.getId().equals(h.getEntidadeId()))
                .findFirst().orElseThrow();
        assertThat(registro.getUsuarioId()).isEqualTo(admin.getId());
        assertThat(registro.getUsuarioNome()).isEqualTo("Admin Autora");
        assertThat(registro.getDescricao()).doesNotContain("senha-secreta-1");
    }
}
