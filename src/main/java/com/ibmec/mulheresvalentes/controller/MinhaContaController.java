package com.ibmec.mulheresvalentes.controller;

import com.ibmec.mulheresvalentes.dto.SenhaForm;
import com.ibmec.mulheresvalentes.exception.RegraNegocioException;
import com.ibmec.mulheresvalentes.model.Usuario;
import com.ibmec.mulheresvalentes.security.UsuarioAutenticado;
import com.ibmec.mulheresvalentes.service.UsuarioService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/minha-conta")
public class MinhaContaController {

    private final UsuarioService usuarioService;

    public MinhaContaController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping("/senha")
    public String formulario(@AuthenticationPrincipal UsuarioAutenticado logado, Model model) {
        model.addAttribute("senhaForm", new SenhaForm());
        return mostrar(logado, model);
    }

    @PostMapping("/senha")
    public String trocar(
            @AuthenticationPrincipal UsuarioAutenticado logado,
            @ModelAttribute("senhaForm") SenhaForm form,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {
        if (form.getNovaSenha() == null || !form.getNovaSenha().equals(form.getConfirmacaoSenha())) {
            bindingResult.rejectValue("confirmacaoSenha", "senha.diferente", "A confirmação não é igual à nova senha.");
            return mostrar(logado, model);
        }
        try {
            usuarioService.trocarPropriaSenha(logado.getId(), form.getSenhaAtual(), form.getNovaSenha());
        } catch (RegraNegocioException ex) {
            bindingResult.reject("senha.invalida", ex.getMessage());
            return mostrar(logado, model);
        }
        redirectAttributes.addFlashAttribute("sucesso", "Senha alterada. Use a nova senha no próximo acesso.");
        return "redirect:/painel";
    }

    private String mostrar(UsuarioAutenticado logado, Model model) {
        Usuario usuario = usuarioService.buscar(logado.getId());
        model.addAttribute("obrigatoria", usuario.isDeveTrocarSenha());
        model.addAttribute("tamanhoMinimo", UsuarioService.TAMANHO_MINIMO_SENHA);
        return "minha-conta/senha";
    }
}
