package com.ibmec.mulheresvalentes.controller;

import com.ibmec.mulheresvalentes.dto.InscricaoForm;
import com.ibmec.mulheresvalentes.exception.RegraNegocioException;
import com.ibmec.mulheresvalentes.model.AreaApoio;
import com.ibmec.mulheresvalentes.model.PreferenciaContato;
import com.ibmec.mulheresvalentes.service.ConferenciaInscricaoService;
import com.ibmec.mulheresvalentes.service.GaleriaService;
import com.ibmec.mulheresvalentes.service.InscricaoService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class PublicoController {

    private final GaleriaService galeriaService;
    private final InscricaoService inscricaoService;
    private final ConferenciaInscricaoService conferenciaService;

    public PublicoController(
            GaleriaService galeriaService,
            InscricaoService inscricaoService,
            ConferenciaInscricaoService conferenciaService) {
        this.galeriaService = galeriaService;
        this.inscricaoService = inscricaoService;
        this.conferenciaService = conferenciaService;
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
        return mostrarFormulario(model);
    }

    @PostMapping("/inscricao")
    public String conferir(
            @Valid @ModelAttribute("inscricaoForm") InscricaoForm form,
            BindingResult bindingResult,
            Model model,
            HttpSession sessao) {
        if (form.isSuspeitaDeRobo()) {
            return "redirect:/inscricao/recebida";
        }
        validarContato(form, bindingResult);
        if (bindingResult.hasErrors()) {
            return mostrarFormulario(model);
        }
        return mostrarConferencia(form, conferenciaService.emitir(sessao), null, model);
    }

    @PostMapping("/inscricao/corrigir")
    public String corrigir(@ModelAttribute("inscricaoForm") InscricaoForm form, Model model) {
        return mostrarFormulario(model);
    }

    @PostMapping("/inscricao/confirmar")
    public String confirmar(
            @Valid @ModelAttribute("inscricaoForm") InscricaoForm form,
            BindingResult bindingResult,
            @RequestParam(name = "conferencia", required = false) String conferencia,
            Model model,
            HttpSession sessao) {
        if (form.isSuspeitaDeRobo()) {
            return "redirect:/inscricao/recebida";
        }
        validarContato(form, bindingResult);
        if (bindingResult.hasErrors()) {
            return mostrarFormulario(model);
        }
        switch (conferenciaService.consumir(sessao, conferencia)) {
            case JA_ENVIADA -> {
                return "redirect:/inscricao/recebida";
            }
            case DESCONHECIDA -> {
                return mostrarConferencia(form, conferenciaService.emitir(sessao),
                        "Passou um tempo desde que você abriu esta página. Confira os dados e confirme de novo.",
                        model);
            }
            case LIBERADA -> {
            }
        }
        try {
            inscricaoService.receber(form);
        } catch (RegraNegocioException ex) {
            conferenciaService.devolver(sessao, conferencia);
            bindingResult.reject("inscricao.invalida", ex.getMessage());
            return mostrarFormulario(model);
        }
        conferenciaService.marcarEnviada(sessao, conferencia);
        return "redirect:/inscricao/recebida";
    }

    @GetMapping("/inscricao/recebida")
    public String recebida() {
        return "publico/inscricao-recebida";
    }

    private void validarContato(InscricaoForm form, BindingResult bindingResult) {
        if (!bindingResult.hasFieldErrors("contato")) {
            InscricaoService.problemaNoContato(form.getContato())
                    .ifPresent(problema -> bindingResult.rejectValue("contato", "contato.invalido", problema));
        }
        if (!bindingResult.hasFieldErrors("contato") && !bindingResult.hasFieldErrors("preferenciaContato")) {
            InscricaoService.problemaNaPreferencia(form.getContato(), form.getPreferenciaContato())
                    .ifPresent(problema -> bindingResult.rejectValue(
                            "preferenciaContato", "preferencia.incompativel", problema));
        }
    }

    private String mostrarFormulario(Model model) {
        model.addAttribute("areasApoio", AreaApoio.values());
        model.addAttribute("preferenciasContato", PreferenciaContato.values());
        return "publico/inscricao";
    }

    private String mostrarConferencia(InscricaoForm form, String conferencia, String aviso, Model model) {
        model.addAttribute("inscricaoForm", form);
        model.addAttribute("contatoFormatado", InscricaoService.formatarContato(form.getContato()));
        model.addAttribute("conferencia", conferencia);
        model.addAttribute("avisoConferencia", aviso);
        return "publico/inscricao-conferir";
    }
}
