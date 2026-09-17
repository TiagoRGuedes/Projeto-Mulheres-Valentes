package com.ibmec.mulheresvalentes.exception;

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
    public ModelAndView naoEncontrado(RecursoNaoEncontradoException ex) {
        return pagina(HttpStatus.NOT_FOUND, "Não encontramos esta informação", ex.getMessage());
    }

    @ExceptionHandler(RegraNegocioException.class)
    public ModelAndView regra(RegraNegocioException ex) {
        return pagina(HttpStatus.UNPROCESSABLE_CONTENT, "Não foi possível concluir", ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ModelAndView inesperado(Exception ex) {
        LOG.log(Level.SEVERE, "Erro inesperado tratado sem exposição ao usuário", ex);
        return pagina(HttpStatus.INTERNAL_SERVER_ERROR, "Algo não saiu como esperado",
                "Tente novamente. Se o problema continuar, peça ajuda à pessoa responsável pelo sistema.");
    }

    private ModelAndView pagina(HttpStatus status, String titulo, String mensagem) {
        ModelAndView view = new ModelAndView("erros/erro");
        view.setStatus(status);
        view.addObject("codigo", status.value());
        view.addObject("tituloErro", titulo);
        view.addObject("mensagemErro", mensagem);
        return view;
    }
}
