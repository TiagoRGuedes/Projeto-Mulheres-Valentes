package com.ibmec.mulheresvalentes.controller;

import com.ibmec.mulheresvalentes.dto.SenhaForm;
import com.ibmec.mulheresvalentes.dto.UsuarioForm;
import com.ibmec.mulheresvalentes.exception.RegraNegocioException;
import com.ibmec.mulheresvalentes.model.PerfilUsuario;
import com.ibmec.mulheresvalentes.model.Usuario;
import com.ibmec.mulheresvalentes.security.UsuarioAutenticado;
import com.ibmec.mulheresvalentes.service.HistoricoOperacaoService;
import com.ibmec.mulheresvalentes.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** Tela de usuários: somente ADMIN (ver SegurancaConfig). */
@Controller
@RequestMapping("/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final HistoricoOperacaoService historicoService;

    public UsuarioController(UsuarioService usuarioService, HistoricoOperacaoService historicoService) {
        this.usuarioService = usuarioService;
        this.historicoService = historicoService;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("usuarios", usuarioService.listar());
        return "usuarios/lista";
    }

    @GetMapping("/novo")
    public String novo(Model model) {
        model.addAttribute("usuarioForm", new UsuarioForm());
        return mostrarFormulario(model, false, null);
    }

    @PostMapping
    public String cadastrar(
            @Valid @ModelAttribute("usuarioForm") UsuarioForm form,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {
        if (form.getSenha() != null && !form.getSenha().equals(form.getConfirmacaoSenha())) {
            bindingResult.rejectValue("confirmacaoSenha", "senha.diferente", "A confirmação não é igual à senha.");
        }
        if (bindingResult.hasErrors()) {
            return mostrarFormulario(model, false, null);
        }
        try {
            Usuario salvo = usuarioService.cadastrar(form.getNome(), form.getLogin(), form.getPerfil(), form.getSenha());
            redirectAttributes.addFlashAttribute("sucesso",
                    "Usuário cadastrado. Passe o login \"" + salvo.getLogin()
                            + "\" e a senha temporária para a pessoa: ela vai criar uma senha nova no primeiro acesso.");
            return "redirect:/usuarios/" + salvo.getId();
        } catch (RegraNegocioException ex) {
            bindingResult.reject("usuario.invalido", ex.getMessage());
            return mostrarFormulario(model, false, null);
        }
    }

    @GetMapping("/{id}")
    public String detalhar(@PathVariable Long id, @AuthenticationPrincipal UsuarioAutenticado logado, Model model) {
        model.addAttribute("usuario", usuarioService.buscar(id));
        model.addAttribute("ehVoce", id.equals(logado.getId()));
        model.addAttribute("senhaForm", new SenhaForm());
        model.addAttribute("tamanhoMinimo", UsuarioService.TAMANHO_MINIMO_SENHA);
        model.addAttribute("historicoEntidade", historicoService.listarPorEntidade("Usuario", id));
        return "usuarios/detalhes";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model) {
        Usuario usuario = usuarioService.buscar(id);
        model.addAttribute("usuarioForm", UsuarioForm.de(usuario));
        return mostrarFormulario(model, true, usuario);
    }

    @PostMapping("/{id}/editar")
    public String atualizar(
            @PathVariable Long id,
            @AuthenticationPrincipal UsuarioAutenticado logado,
            @Valid @ModelAttribute("usuarioForm") UsuarioForm form,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {
        Usuario usuario = usuarioService.buscar(id);
        if (bindingResult.hasErrors()) {
            return mostrarFormulario(model, true, usuario);
        }
        try {
            usuarioService.atualizar(id, form.getNome(), form.getPerfil(), logado.getId());
        } catch (RegraNegocioException ex) {
            bindingResult.reject("usuario.invalido", ex.getMessage());
            return mostrarFormulario(model, true, usuario);
        }
        redirectAttributes.addFlashAttribute("sucesso", "Dados do usuário atualizados.");
        return "redirect:/usuarios/" + id;
    }

    @PostMapping("/{id}/senha")
    public String redefinirSenha(
            @PathVariable Long id,
            @ModelAttribute("senhaForm") SenhaForm form,
            RedirectAttributes redirectAttributes) {
        if (form.getNovaSenha() == null || !form.getNovaSenha().equals(form.getConfirmacaoSenha())) {
            redirectAttributes.addFlashAttribute("erro", "A confirmação não é igual à nova senha. Nada foi alterado.");
            return "redirect:/usuarios/" + id;
        }
        try {
            usuarioService.redefinirSenha(id, form.getNovaSenha());
        } catch (RegraNegocioException ex) {
            redirectAttributes.addFlashAttribute("erro", ex.getMessage());
            return "redirect:/usuarios/" + id;
        }
        redirectAttributes.addFlashAttribute("sucesso",
                "Senha temporária definida. Passe para a pessoa: ela vai criar uma senha nova no próximo acesso.");
        return "redirect:/usuarios/" + id;
    }

    @PostMapping("/{id}/desativar")
    public String desativar(
            @PathVariable Long id,
            @AuthenticationPrincipal UsuarioAutenticado logado,
            RedirectAttributes redirectAttributes) {
        try {
            usuarioService.desativar(id, logado.getId());
            redirectAttributes.addFlashAttribute("sucesso", "Usuário desativado. Ele não consegue mais entrar no painel.");
        } catch (RegraNegocioException ex) {
            redirectAttributes.addFlashAttribute("erro", ex.getMessage());
        }
        return "redirect:/usuarios/" + id;
    }

    @PostMapping("/{id}/ativar")
    public String ativar(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        usuarioService.ativar(id);
        redirectAttributes.addFlashAttribute("sucesso", "Usuário reativado. Ele já pode entrar no painel.");
        return "redirect:/usuarios/" + id;
    }

    private String mostrarFormulario(Model model, boolean modoEdicao, Usuario usuario) {
        model.addAttribute("modoEdicao", modoEdicao);
        model.addAttribute("usuario", usuario);
        model.addAttribute("perfis", PerfilUsuario.values());
        model.addAttribute("tamanhoMinimo", UsuarioService.TAMANHO_MINIMO_SENHA);
        return "usuarios/formulario";
    }
}
