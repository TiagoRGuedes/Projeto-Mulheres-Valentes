package com.ibmec.mulheresvalentes.controller;

import com.ibmec.mulheresvalentes.dto.PreferenciasAcessibilidade;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AcessibilidadeController {

    public static final String CHAVE_SESSAO = "preferenciasAcessibilidade";

    @GetMapping("/acessibilidade")
    public String pagina() {
        return "acessibilidade";
    }

    @PostMapping("/acessibilidade")
    public String salvar(
            @RequestParam(defaultValue = "normal") String tamanhoFonte,
            @RequestParam(defaultValue = "false") boolean altoContraste,
            @RequestParam(defaultValue = "false") boolean modoEscuro,
            @RequestParam(defaultValue = "false") boolean reduzirAnimacoes,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        session.setAttribute(CHAVE_SESSAO, new PreferenciasAcessibilidade(
                tamanhoFonte, altoContraste, modoEscuro, reduzirAnimacoes));
        redirectAttributes.addFlashAttribute("sucesso", "Preferências de acessibilidade salvas.");
        return "redirect:/acessibilidade";
    }

    @PostMapping("/acessibilidade/restaurar")
    public String restaurar(HttpSession session, RedirectAttributes redirectAttributes) {
        session.setAttribute(CHAVE_SESSAO, PreferenciasAcessibilidade.padrao());
        redirectAttributes.addFlashAttribute("sucesso", "Preferências restauradas para o padrão.");
        return "redirect:/acessibilidade";
    }
}
