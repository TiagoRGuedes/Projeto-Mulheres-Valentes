package com.ibmec.mulheresvalentes.controller;

import com.ibmec.mulheresvalentes.dto.InscricaoForm;
import com.ibmec.mulheresvalentes.exception.RegraNegocioException;
import com.ibmec.mulheresvalentes.model.AreaApoio;
import com.ibmec.mulheresvalentes.model.PreferenciaContato;
import com.ibmec.mulheresvalentes.service.GaleriaService;
import com.ibmec.mulheresvalentes.service.InscricaoService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class PublicoController {

    private final GaleriaService galeriaService;
    private final InscricaoService inscricaoService;

    public PublicoController(GaleriaService galeriaService, InscricaoService inscricaoService) {
        this.galeriaService = galeriaService;
        this.inscricaoService = inscricaoService;
    }

    @GetMapping("/")
    public String inicio(Model model) {
        model.addAttribute("galeria", galeriaService.listar());
        return "publico/inicio";
    }

    @GetMapping("/inscricao")
    public String formulario(Model model) {
        if (!model.containsAttribute("inscricaoForm")) {
            model.addAttribute("inscricaoForm", new InscricaoForm());
        }
        prepararFormulario(model);
        return "publico/inscricao";
    }

    @PostMapping("/inscricao")
    public String enviar(
            @Valid @ModelAttribute("inscricaoForm") InscricaoForm form,
            BindingResult bindingResult,
            Model model) {
        if (form.isSuspeitaDeRobo()) {
            return "redirect:/inscricao/recebida";
        }
        if (bindingResult.hasErrors()) {
            prepararFormulario(model);
            return "publico/inscricao";
        }
        try {
            inscricaoService.receber(form);
        } catch (RegraNegocioException ex) {
            bindingResult.reject("inscricao.invalida", ex.getMessage());
            prepararFormulario(model);
            return "publico/inscricao";
        }
        return "redirect:/inscricao/recebida";
    }

    @GetMapping("/inscricao/recebida")
    public String recebida() {
        return "publico/inscricao-recebida";
    }

    private void prepararFormulario(Model model) {
        model.addAttribute("areasApoio", AreaApoio.values());
        model.addAttribute("preferenciasContato", PreferenciaContato.values());
    }
}
