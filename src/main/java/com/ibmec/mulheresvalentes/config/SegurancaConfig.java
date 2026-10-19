package com.ibmec.mulheresvalentes.config;

import com.ibmec.mulheresvalentes.security.LoginSucessoHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

import java.util.Arrays;

/**
 * Regras de acesso (RNF04).
 *
 * <ul>
 *   <li>Página pública e inscrição: abertas para qualquer pessoa.</li>
 *   <li>Painel da equipe: exige login (perfis ADMIN ou OPERADOR).</li>
 *   <li>Usuários, Histórico do sistema, categorias, exclusões e console do banco: somente ADMIN.</li>
 * </ul>
 */
@Configuration
@EnableWebSecurity
public class SegurancaConfig {

    /** Endereços que qualquer pessoa pode abrir, sem login. */
    static final String[] PUBLICOS = {
        "/", "/inscricao", "/inscricao/**",
        "/login", "/error",
        "/css/**", "/fonts/**", "/img/**", "/favicon.ico"
    };

    /** Endereços reservados ao perfil ADMIN. */
    static final String[] SOMENTE_ADMIN = {
        "/usuarios", "/usuarios/**",
        "/historico", "/historico/**",
        "/caixa/categorias", "/caixa/categorias/**",
        "/participantes/*/excluir",
        "/atividades/*/excluir",
        "/encontros/*/excluir",
        "/caixa/movimentacoes/*/excluir",
        "/h2-console", "/h2-console/**"
    };

    @Bean
    public SecurityFilterChain filtroSeguranca(HttpSecurity http, LoginSucessoHandler loginSucesso) throws Exception {
        PathPatternRequestMatcher.Builder caminho = PathPatternRequestMatcher.withDefaults();

        http
            .authorizeHttpRequests(acesso -> acesso
                .requestMatchers(matchers(PUBLICOS)).permitAll()
                .requestMatchers(matchers(SOMENTE_ADMIN)).hasRole("ADMIN")
                .requestMatchers(caminho.matcher(HttpMethod.DELETE, "/api/**")).hasRole("ADMIN")
                .anyRequest().authenticated())
            .formLogin(login -> login
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .usernameParameter("login")
                .passwordParameter("senha")
                .successHandler(loginSucesso)
                .failureUrl("/login?erro")
                .permitAll())
            .logout(sair -> sair
                .logoutRequestMatcher(caminho.matcher(HttpMethod.POST, "/sair"))
                .logoutSuccessUrl("/login?saiu")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll())
            // A API pode ser usada por scripts com usuário e senha (HTTP Basic).
            .httpBasic(Customizer.withDefaults())
            .exceptionHandling(erros -> erros
                .defaultAuthenticationEntryPointFor(
                    new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED), caminho.matcher("/api/**")))
            // A API recebe JSON (exige preflight CORS) e o console H2 tem formulários próprios.
            .csrf(csrf -> csrf.ignoringRequestMatchers(matchers("/api/**", "/h2-console/**")))
            // O console H2 usa frames da própria aplicação.
            .headers(cabecalhos -> cabecalhos.frameOptions(frames -> frames.sameOrigin()));

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        // BCrypt por padrão, com prefixo {bcrypt} para permitir trocar o algoritmo no futuro.
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    private static RequestMatcher[] matchers(String... padroes) {
        PathPatternRequestMatcher.Builder caminho = PathPatternRequestMatcher.withDefaults();
        return Arrays.stream(padroes).map(caminho::matcher).toArray(RequestMatcher[]::new);
    }
}
