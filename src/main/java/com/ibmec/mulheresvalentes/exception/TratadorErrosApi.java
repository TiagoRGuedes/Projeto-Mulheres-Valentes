package com.ibmec.mulheresvalentes.exception;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice(basePackages = "com.ibmec.mulheresvalentes.controller.api")
public class TratadorErrosApi {

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<ErroApi> naoEncontrado(RecursoNaoEncontradoException ex) {
        return resposta(HttpStatus.NOT_FOUND, ex.getMessage(), List.of());
    }

    @ExceptionHandler(RegistroDuplicadoException.class)
    public ResponseEntity<ErroApi> duplicado(RegistroDuplicadoException ex) {
        return resposta(HttpStatus.CONFLICT, ex.getMessage(), List.of());
    }

    @ExceptionHandler(RegraNegocioException.class)
    public ResponseEntity<ErroApi> regra(RegraNegocioException ex) {
        return resposta(HttpStatus.UNPROCESSABLE_CONTENT, ex.getMessage(), List.of());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroApi> validacao(MethodArgumentNotValidException ex) {
        List<String> detalhes = ex.getBindingResult().getFieldErrors().stream()
                .map(erro -> erro.getDefaultMessage() == null ? "Revise o campo informado." : erro.getDefaultMessage())
                .distinct()
                .toList();
        return resposta(HttpStatus.BAD_REQUEST, "Revise os dados enviados.", detalhes);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErroApi> argumento(IllegalArgumentException ex) {
        return resposta(HttpStatus.BAD_REQUEST, mensagemSegura(ex), List.of());
    }

    private ResponseEntity<ErroApi> resposta(HttpStatus status, String mensagem, List<String> detalhes) {
        return ResponseEntity.status(status).body(new ErroApi(
                LocalDateTime.now(), status.value(), status.getReasonPhrase(), mensagem, detalhes));
    }

    private String mensagemSegura(Exception ex) {
        return ex.getMessage() == null || ex.getMessage().isBlank()
                ? "Não foi possível processar os dados enviados."
                : ex.getMessage();
    }

    public record ErroApi(LocalDateTime instante, int status, String erro, String mensagem, List<String> detalhes) {
    }
}
