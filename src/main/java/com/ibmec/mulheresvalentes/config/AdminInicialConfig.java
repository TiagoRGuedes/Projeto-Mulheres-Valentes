package com.ibmec.mulheresvalentes.config;

import com.ibmec.mulheresvalentes.service.UsuarioService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;

/**
 * Cria a primeira conta de administração quando ainda não existe nenhum usuário.
 * Se nenhuma senha for configurada em app.admin-inicial.senha, gera uma senha
 * aleatória e mostra no terminal uma única vez. No primeiro acesso, o sistema
 * obriga a troca dessa senha.
 */
@Component
@Order(0)
public class AdminInicialConfig implements ApplicationRunner {

    private static final Logger LOG = LoggerFactory.getLogger(AdminInicialConfig.class);
    private static final String CARACTERES = "abcdefghjkmnpqrstuvwxyzABCDEFGHJKLMNPQRSTUVWXYZ23456789";

    private final UsuarioService usuarioService;
    private final String nome;
    private final String login;
    private final String senhaConfigurada;
    private final boolean redefinir;

    public AdminInicialConfig(
            UsuarioService usuarioService,
            @Value("${app.admin-inicial.nome:Administração}") String nome,
            @Value("${app.admin-inicial.login:admin}") String login,
            @Value("${app.admin-inicial.senha:}") String senhaConfigurada,
            @Value("${app.admin-inicial.redefinir:false}") boolean redefinir) {
        this.usuarioService = usuarioService;
        this.nome = nome;
        this.login = login;
        this.senhaConfigurada = senhaConfigurada;
        this.redefinir = redefinir;
    }

    @Override
    public void run(ApplicationArguments args) {
        boolean gerada = senhaConfigurada == null || senhaConfigurada.isBlank();
        String senha = gerada ? gerarSenha() : senhaConfigurada;
        if (redefinir) {
            usuarioService.recuperarAdministracao(nome, login, senha);
            LOG.warn("""

                    ================================================================
                     RECUPERAÇÃO DE ACESSO: nova senha temporária da administração.
                       Login: {}
                       Senha: {}
                     Entre em http://localhost:8080/login e crie uma senha nova.
                     IMPORTANTE: da próxima vez, inicie o sistema SEM a opção
                     app.admin-inicial.redefinir=true (senão a senha muda de novo).
                    ================================================================
                    """, UsuarioService.normalizarLogin(login),
                    gerada ? senha : "(a definida em app.admin-inicial.senha)");
            return;
        }
        if (usuarioService.criarAdministracaoInicialSeNecessario(nome, login, senha)) {
            LOG.warn("""

                    ================================================================
                     Primeira conta de administração criada.
                       Login: {}
                       Senha: {}
                     Entre em http://localhost:8080/login. No primeiro acesso o
                     sistema vai pedir para você criar uma senha nova.
                    ================================================================
                    """, UsuarioService.normalizarLogin(login),
                    gerada ? senha : "(a definida em app.admin-inicial.senha)");
        }
    }

    private static String gerarSenha() {
        SecureRandom aleatorio = new SecureRandom();
        StringBuilder senha = new StringBuilder();
        for (int i = 0; i < 12; i++) {
            senha.append(CARACTERES.charAt(aleatorio.nextInt(CARACTERES.length())));
        }
        return senha.toString();
    }
}
