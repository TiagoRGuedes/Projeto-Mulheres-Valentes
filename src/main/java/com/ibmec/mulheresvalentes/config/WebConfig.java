package com.ibmec.mulheresvalentes.config;

import com.ibmec.mulheresvalentes.security.AutenticacaoAdminInterceptor;
import com.ibmec.mulheresvalentes.security.ConsoleH2Filter;
import com.ibmec.mulheresvalentes.security.SessaoAdministrador;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final AutenticacaoAdminInterceptor autenticacaoAdminInterceptor;

    public WebConfig(AutenticacaoAdminInterceptor autenticacaoAdminInterceptor) {
        this.autenticacaoAdminInterceptor = autenticacaoAdminInterceptor;
    }

    /**
     * Fecha o console do H2 para quem não entrou pelo {@code /admin}.
     */
    @Bean
    public FilterRegistrationBean<ConsoleH2Filter> protecaoDoConsoleH2(
            SessaoAdministrador sessaoAdministrador,
            @Value("${spring.h2.console.path:/h2-console}") String caminhoDoConsole) {
        FilterRegistrationBean<ConsoleH2Filter> registro = new FilterRegistrationBean<>();
        registro.setFilter(new ConsoleH2Filter(sessaoAdministrador));
        registro.addUrlPatterns(caminhoDoConsole, caminhoDoConsole + "/*");
        return registro;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registro) {
        registro.addInterceptor(autenticacaoAdminInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/css/**", "/js/**", "/img/**", "/fonts/**", "/favicon.ico");
    }
}
