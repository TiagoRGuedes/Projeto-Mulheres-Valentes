package com.ibmec.mulheresvalentes.controller;

import com.ibmec.mulheresvalentes.security.UsuarioLogado;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginController {

    @GetMapping("/login")
    public String login() {
        if (UsuarioLogado.atual().isPresent()) {
            return "redirect:/painel";
        }
        return "auth/login";
    }
}
