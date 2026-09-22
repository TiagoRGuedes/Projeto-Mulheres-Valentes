package com.ibmec.mulheresvalentes.exception;

import com.ibmec.mulheresvalentes.controller.AcessibilidadeController;
import com.ibmec.mulheresvalentes.dto.PreferenciasAcessibilidade;
import jakarta.servlet.http.HttpSession;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

@Order(Ordered.LOWEST_PRECEDENCE)
@ControllerAdvice(basePackages = "com.ibmec.mulheresvalentes.controller")
public class TratadorErrosWeb {

    private static final Logger LOG = Logger.getLogger(TratadorErrosWeb.class.getName());

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ModelAndView naoEncontrado(RecursoNaoEncontradoException ex, HttpSession sessao) {
        return pagina(sessao, HttpStatus.NOT_FOUND, "Não encontramos esta informação", ex.getMessage());
    }

    @ExceptionHandler(RegraNegocioException.class)
    public ModelAndView regra(RegraNegocioException ex, HttpSession sessao) {
        return pagina(sessao, HttpStatus.UNPROCESSABLE_CONTENT, "Não foi possível concluir", ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ModelAndView inesperado(Exception ex, HttpSession sessao) {
        LOG.log(Level.SEVERE, "Erro inesperado tratado sem exposição ao usuário", ex);
        return pagina(sessao, HttpStatus.INTERNAL_SERVER_ERROR, "Algo não saiu como esperado",
                "Tente novamente. Se o problema continuar, peça ajuda à pessoa responsável pelo sistema.");
    }

    private ModelAndView pagina(HttpSession sessao, HttpStatus status, String titulo, String mensagem) {
        ModelAndView view = new ModelAndView("erros/erro");
        view.setStatus(status);
        view.addObject("codigo", status.value());
        view.addObject("tituloErro", titulo);
        view.addObject("mensagemErro", mensagem);
        view.addObject("preferencias", preferencias(sessao));
        return view;
    }

    private PreferenciasAcessibilidade preferencias(HttpSession sessao) {
        if (sessao != null
                && sessao.getAttribute(AcessibilidadeController.CHAVE_SESSAO)
                        instanceof PreferenciasAcessibilidade preferencias) {
            return preferencias;
        }
        return PreferenciasAcessibilidade.padrao();
    }
}
