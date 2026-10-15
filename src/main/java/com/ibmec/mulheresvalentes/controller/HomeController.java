package com.ibmec.mulheresvalentes.controller;

import com.ibmec.mulheresvalentes.service.DashboardService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    private final DashboardService dashboardService;

    public HomeController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/painel")
    public String home(Model model) {
        model.addAttribute("dashboard", dashboardService.carregar());
        return "index";
    }
}
