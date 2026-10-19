package com.ibmec.mulheresvalentes.controller;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.webmvc.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class ErroController implements ErrorController {

    @RequestMapping("/error")
    public String erro(HttpServletRequest request, Model model) {
        int codigo = obterCodigo(request);
        boolean naoEncontrado = codigo == 404;
        model.addAttribute("codigo", codigo);
        if (codigo == 403) {
            model.addAttribute("tituloErro", "Esta área é só para a administração");
            model.addAttribute("mensagemErro",
                    "O seu perfil não tem permissão para abrir esta página. Se precisar fazer isto, fale com uma pessoa administradora.");
            return "erros/erro";
        }
        model.addAttribute("tituloErro", naoEncontrado ? "Página não encontrada" : "Algo não saiu como esperado");
        model.addAttribute("mensagemErro", naoEncontrado
                ? "O endereço informado não existe ou pode ter mudado."
                : "Tente novamente. Se o problema continuar, peça ajuda à pessoa responsável pelo sistema.");
        return "erros/erro";
    }

    private int obterCodigo(HttpServletRequest request) {
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        if (status instanceof Integer codigo) {
            return codigo;
        }
        return 500;
    }
}
