package com.ibmec.mulheresvalentes.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final TrocaSenhaInterceptor trocaSenhaInterceptor;

    public WebConfig(TrocaSenhaInterceptor trocaSenhaInterceptor) {
        this.trocaSenhaInterceptor = trocaSenhaInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(trocaSenhaInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        TrocaSenhaInterceptor.PAGINA_TROCA,
                        "/", "/inscricao", "/inscricao/**",
                        "/login", "/sair", "/error",
                        "/acessibilidade", "/acessibilidade/**",
                        "/css/**", "/fonts/**", "/img/**", "/favicon.ico");
    }
}
