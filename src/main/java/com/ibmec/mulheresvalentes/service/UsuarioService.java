package com.ibmec.mulheresvalentes.service;

import com.ibmec.mulheresvalentes.exception.RecursoNaoEncontradoException;
import com.ibmec.mulheresvalentes.exception.RegraNegocioException;
import com.ibmec.mulheresvalentes.model.AcaoHistorico;
import com.ibmec.mulheresvalentes.model.ModuloHistorico;
import com.ibmec.mulheresvalentes.model.PerfilUsuario;
import com.ibmec.mulheresvalentes.model.Usuario;
import com.ibmec.mulheresvalentes.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Regras das contas da equipe. Toda alteração fica no Histórico do sistema,
 * mas nenhuma senha (nem o hash) é escrita nele.
 */
@Service
public class UsuarioService {

    public static final int TAMANHO_MINIMO_SENHA = 8;
    private static final Pattern FORMATO_LOGIN = Pattern.compile("[a-z0-9._-]{3,40}");

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final HistoricoOperacaoService historicoService;

    public UsuarioService(
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder,
            HistoricoOperacaoService historicoService) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.historicoService = historicoService;
    }

    @Transactional(readOnly = true)
    public List<Usuario> listar() {
        return usuarioRepository.findAllByOrderByAtivoDescNomeAsc();
    }

    @Transactional(readOnly = true)
    public Usuario buscar(Long id) {
        if (id == null) {
            throw new RecursoNaoEncontradoException("Usuário não encontrado.");
        }
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado."));
    }

    @Transactional
    public Usuario cadastrar(String nome, String login, PerfilUsuario perfil, String senhaTemporaria) {
        String loginNormalizado = validarLogin(login);
        if (usuarioRepository.existsByLogin(loginNormalizado)) {
            throw new RegraNegocioException("Já existe um usuário com o login \"" + loginNormalizado + "\". Escolha outro.");
        }
        validarSenha(senhaTemporaria);
        Usuario usuario = new Usuario(
                validarNome(nome), loginNormalizado, passwordEncoder.encode(senhaTemporaria), exigirPerfil(perfil));
        Usuario salvo = usuarioRepository.save(usuario);
        historicoService.registrar(
                ModuloHistorico.USUARIOS, "Usuario", salvo.getId(), salvo.getNome(),
                AcaoHistorico.CRIACAO,
                "Usuário \"" + salvo.getLogin() + "\" cadastrado com perfil " + salvo.getPerfil().getDescricao() + ".");
        return salvo;
    }

    /**
     * Cria a primeira conta de administração quando o banco ainda não tem usuários.
     * Não passa pelo Histórico porque não existe ninguém logado nesse momento.
     */
    @Transactional
    public boolean criarAdministracaoInicialSeNecessario(String nome, String login, String senha) {
        if (usuarioRepository.count() > 0) {
            return false;
        }
        validarSenha(senha);
        usuarioRepository.save(new Usuario(
                validarNome(nome), validarLogin(login), passwordEncoder.encode(senha), PerfilUsuario.ADMIN));
        return true;
    }

    /**
     * Recuperação de acesso: define uma senha temporária para a conta de administração
     * informada (reativando-a e garantindo o perfil ADMIN). Se a conta não existir, cria.
     * Usada só na inicialização, com app.admin-inicial.redefinir=true.
     */
    @Transactional
    public Usuario recuperarAdministracao(String nome, String login, String senhaTemporaria) {
        String loginNormalizado = validarLogin(login);
        validarSenha(senhaTemporaria);
        Usuario usuario = usuarioRepository.findByLogin(loginNormalizado).orElse(null);
        if (usuario == null) {
            usuario = usuarioRepository.save(new Usuario(
                    validarNome(nome), loginNormalizado, passwordEncoder.encode(senhaTemporaria), PerfilUsuario.ADMIN));
        } else {
            usuario.atualizarDados(usuario.getNome(), PerfilUsuario.ADMIN);
            usuario.ativar();
            usuario.definirSenhaTemporaria(passwordEncoder.encode(senhaTemporaria));
            usuario = usuarioRepository.save(usuario);
        }
        historicoService.registrar(
                ModuloHistorico.USUARIOS, "Usuario", usuario.getId(), usuario.getNome(),
                AcaoHistorico.EDICAO,
                "Acesso de administração recuperado na inicialização do sistema (senha temporária gerada).");
        return usuario;
    }

    @Transactional
    public Usuario atualizar(Long id, String nome, PerfilUsuario perfil, Long idQuemAltera) {
        Usuario usuario = buscar(id);
        PerfilUsuario novoPerfil = exigirPerfil(perfil);
        if (usuario.isAdmin() && novoPerfil != PerfilUsuario.ADMIN) {
            if (Objects.equals(id, idQuemAltera)) {
                throw new RegraNegocioException("Você não pode tirar o seu próprio perfil de administração. Peça a outra pessoa administradora.");
            }
            garantirOutroAdminAtivo(usuario);
        }
        String anterior = resumo(usuario);
        usuario.atualizarDados(validarNome(nome), novoPerfil);
        Usuario salvo = usuarioRepository.save(usuario);
        historicoService.registrar(
                ModuloHistorico.USUARIOS, "Usuario", salvo.getId(), salvo.getNome(),
                AcaoHistorico.EDICAO, "Dados do usuário atualizados.", anterior, resumo(salvo));
        return salvo;
    }

    @Transactional
    public Usuario desativar(Long id, Long idQuemAltera) {
        Usuario usuario = buscar(id);
        if (!usuario.isAtivo()) {
            return usuario;
        }
        if (Objects.equals(id, idQuemAltera)) {
            throw new RegraNegocioException("Você não pode desativar a sua própria conta.");
        }
        if (usuario.isAdmin()) {
            garantirOutroAdminAtivo(usuario);
        }
        usuario.desativar();
        Usuario salvo = usuarioRepository.save(usuario);
        historicoService.registrar(
                ModuloHistorico.USUARIOS, "Usuario", salvo.getId(), salvo.getNome(),
                AcaoHistorico.DESATIVACAO, "Usuário \"" + salvo.getLogin() + "\" desativado: não consegue mais entrar no painel.");
        return salvo;
    }

    @Transactional
    public Usuario ativar(Long id) {
        Usuario usuario = buscar(id);
        if (usuario.isAtivo()) {
            return usuario;
        }
        usuario.ativar();
        Usuario salvo = usuarioRepository.save(usuario);
        historicoService.registrar(
                ModuloHistorico.USUARIOS, "Usuario", salvo.getId(), salvo.getNome(),
                AcaoHistorico.ATIVACAO, "Usuário \"" + salvo.getLogin() + "\" reativado.");
        return salvo;
    }

    /** A administração define uma senha temporária; a pessoa troca no próximo acesso. */
    @Transactional
    public Usuario redefinirSenha(Long id, String senhaTemporaria) {
        Usuario usuario = buscar(id);
        validarSenha(senhaTemporaria);
        usuario.definirSenhaTemporaria(passwordEncoder.encode(senhaTemporaria));
        Usuario salvo = usuarioRepository.save(usuario);
        historicoService.registrar(
                ModuloHistorico.USUARIOS, "Usuario", salvo.getId(), salvo.getNome(),
                AcaoHistorico.EDICAO, "Senha redefinida pela administração. A pessoa precisa criar uma nova no próximo acesso.");
        return salvo;
    }

    /** A própria pessoa troca a senha, informando a atual. */
    @Transactional
    public void trocarPropriaSenha(Long id, String senhaAtual, String novaSenha) {
        Usuario usuario = buscar(id);
        if (senhaAtual == null || !passwordEncoder.matches(senhaAtual, usuario.getSenhaHash())) {
            throw new RegraNegocioException("A senha atual não confere.");
        }
        validarSenha(novaSenha);
        if (passwordEncoder.matches(novaSenha, usuario.getSenhaHash())) {
            throw new RegraNegocioException("A nova senha precisa ser diferente da atual.");
        }
        usuario.definirSenhaPropria(passwordEncoder.encode(novaSenha));
        usuarioRepository.save(usuario);
        historicoService.registrar(
                ModuloHistorico.USUARIOS, "Usuario", usuario.getId(), usuario.getNome(),
                AcaoHistorico.EDICAO, "A pessoa trocou a própria senha.");
    }

    @Transactional
    public void registrarAcesso(Long id) {
        usuarioRepository.findById(id).ifPresent(usuario -> {
            usuario.registrarAcesso();
            usuarioRepository.save(usuario);
        });
    }

    public static String normalizarLogin(String login) {
        return login == null ? "" : login.trim().toLowerCase(Locale.ROOT);
    }

    private void garantirOutroAdminAtivo(Usuario usuario) {
        long adminsAtivos = usuarioRepository.countByPerfilAndAtivoTrue(PerfilUsuario.ADMIN);
        long outros = usuario.isAtivo() ? adminsAtivos - 1 : adminsAtivos;
        if (outros < 1) {
            throw new RegraNegocioException("O sistema precisa de pelo menos uma pessoa administradora ativa. Cadastre ou promova outra antes.");
        }
    }

    private String validarNome(String nome) {
        if (nome == null || nome.isBlank()) {
            throw new RegraNegocioException("Informe o nome da pessoa.");
        }
        String normalizado = nome.trim().replaceAll("\\s+", " ");
        if (normalizado.length() > 150) {
            throw new RegraNegocioException("O nome deve ter no máximo 150 caracteres.");
        }
        return normalizado;
    }

    private String validarLogin(String login) {
        String normalizado = normalizarLogin(login);
        if (!FORMATO_LOGIN.matcher(normalizado).matches()) {
            throw new RegraNegocioException(
                    "O login deve ter de 3 a 40 caracteres, usando só letras sem acento, números, ponto, hífen ou sublinhado.");
        }
        return normalizado;
    }

    private void validarSenha(String senha) {
        if (senha == null || senha.length() < TAMANHO_MINIMO_SENHA) {
            throw new RegraNegocioException("A senha precisa ter pelo menos " + TAMANHO_MINIMO_SENHA + " caracteres.");
        }
        if (senha.getBytes(java.nio.charset.StandardCharsets.UTF_8).length > 72) {
            throw new RegraNegocioException("A senha deve ter no máximo 72 caracteres.");
        }
        if (senha.isBlank()) {
            throw new RegraNegocioException("A senha não pode ser feita só de espaços.");
        }
    }

    private PerfilUsuario exigirPerfil(PerfilUsuario perfil) {
        if (perfil == null) {
            throw new RegraNegocioException("Escolha o perfil de acesso.");
        }
        return perfil;
    }

    private String resumo(Usuario usuario) {
        return "Nome: " + usuario.getNome() + " · Perfil: " + usuario.getPerfil().getDescricao();
    }
}
