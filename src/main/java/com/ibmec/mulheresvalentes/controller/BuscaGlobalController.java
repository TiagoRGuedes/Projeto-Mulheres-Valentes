package com.ibmec.mulheresvalentes.controller;

import com.ibmec.mulheresvalentes.service.BuscaGlobalService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class BuscaGlobalController {

    private final BuscaGlobalService buscaGlobalService;

    public BuscaGlobalController(BuscaGlobalService buscaGlobalService) {
        this.buscaGlobalService = buscaGlobalService;
    }

    @GetMapping("/busca")
    public String buscar(
            @RequestParam(name = "q", required = false) String termo,
            Model model) {
        model.addAttribute("resultado", buscaGlobalService.buscar(termo));
        return "busca/resultados";
    }
}
