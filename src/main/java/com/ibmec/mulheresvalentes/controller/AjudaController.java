package com.ibmec.mulheresvalentes.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AjudaController {

    @GetMapping("/ajuda")
    public String ajuda() {
        return "ajuda/index";
    }
}
