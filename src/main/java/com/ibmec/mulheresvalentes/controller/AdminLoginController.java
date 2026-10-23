package com.ibmec.mulheresvalentes.controller;

import com.ibmec.mulheresvalentes.security.SessaoAdministrador;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/admin")
public class AdminLoginController {

    private final SessaoAdministrador sessaoAdministrador;

    public AdminLoginController(SessaoAdministrador sessaoAdministrador) {
        this.sessaoAdministrador = sessaoAdministrador;
    }

    @GetMapping
    public String formulario(
            @RequestParam(name = "saiu", required = false) String saiu,
            HttpSession sessao,
            Model model) {
        if (sessaoAdministrador.autenticado(sessao)) {
            return "redirect:/painel";
        }
        if (saiu != null) {
            model.addAttribute("aviso", "Você saiu do painel. Entre de novo para continuar.");
        }
        return "admin/login";
    }

    @PostMapping
    public String entrar(
            @RequestParam(name = "usuario", required = false) String usuario,
            @RequestParam(name = "senha", required = false) String senha,
            HttpServletRequest requisicao,
            Model model) {
        if (sessaoAdministrador.entrar(requisicao, usuario, senha)) {
            return "redirect:/painel";
        }
        model.addAttribute("erro", "Usuário ou senha não conferem. Tente de novo.");
        model.addAttribute("usuario", usuario);
        return "admin/login";
    }

    @PostMapping("/sair")
    public String sair(HttpSession sessao) {
        sessaoAdministrador.sair(sessao);
        return "redirect:/admin?saiu";
    }
}
