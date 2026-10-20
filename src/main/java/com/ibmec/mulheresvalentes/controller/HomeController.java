package com.ibmec.mulheresvalentes.controller;

import com.ibmec.mulheresvalentes.service.ContatoInscricaoService;
import com.ibmec.mulheresvalentes.service.DashboardService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    private final DashboardService dashboardService;
    private final ContatoInscricaoService contatoService;
    private final int diasSemContato;

    public HomeController(
            DashboardService dashboardService,
            ContatoInscricaoService contatoService,
            @Value("${app.inscricoes.dias-sem-contato:3}") int diasSemContato) {
        this.dashboardService = dashboardService;
        this.contatoService = contatoService;
        this.diasSemContato = diasSemContato;
    }

    @GetMapping("/painel")
    public String home(Model model) {
        model.addAttribute("dashboard", dashboardService.carregar());
        model.addAttribute("inscricoesAtrasadas", contatoService.esperandoPrimeiroContato(diasSemContato).size());
        model.addAttribute("diasSemContato", diasSemContato);
        return "index";
    }
}
