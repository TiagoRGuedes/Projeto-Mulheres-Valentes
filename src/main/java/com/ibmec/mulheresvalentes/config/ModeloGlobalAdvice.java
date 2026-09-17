package com.ibmec.mulheresvalentes.config;

import com.ibmec.mulheresvalentes.controller.AcessibilidadeController;
import com.ibmec.mulheresvalentes.dto.PreferenciasAcessibilidade;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class ModeloGlobalAdvice {

    @ModelAttribute("preferencias")
    public PreferenciasAcessibilidade preferencias(HttpSession session) {
        Object valor = session.getAttribute(AcessibilidadeController.CHAVE_SESSAO);
        if (valor instanceof PreferenciasAcessibilidade preferencias) {
            return preferencias;
        }
        PreferenciasAcessibilidade padrao = PreferenciasAcessibilidade.padrao();
        session.setAttribute(AcessibilidadeController.CHAVE_SESSAO, padrao);
        return padrao;
    }
}
