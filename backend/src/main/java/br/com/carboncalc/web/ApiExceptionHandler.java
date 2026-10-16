package br.com.carboncalc.web;

import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import br.com.carboncalc.domain.NaoEncontradoException;
import br.com.carboncalc.domain.RegraNegocioException;
import software.amazon.awssdk.core.exception.SdkException;

@RestControllerAdvice
public class ApiExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

    public record Erro(String mensagem) {
    }

    @ExceptionHandler(RegraNegocioException.class)
    ResponseEntity<Erro> regraNegocio(RegraNegocioException e) {
        return resposta(HttpStatus.UNPROCESSABLE_ENTITY, e.getMessage());
    }

    @ExceptionHandler(NaoEncontradoException.class)
    ResponseEntity<Erro> naoEncontrado(NaoEncontradoException e) {
        return resposta(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler(BadCredentialsException.class)
    ResponseEntity<Erro> credenciais(BadCredentialsException e) {
        return resposta(HttpStatus.UNAUTHORIZED, e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<Erro> validacao(MethodArgumentNotValidException e) {
        String mensagem = e.getBindingResult().getFieldErrors().stream()
                .map(f -> f.getField() + ": " + f.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return resposta(HttpStatus.BAD_REQUEST, mensagem);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    ResponseEntity<Erro> corpoInvalido(HttpMessageNotReadableException e) {
        return resposta(HttpStatus.BAD_REQUEST, "O corpo da requisição é inválido");
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    ResponseEntity<Erro> integridade(DataIntegrityViolationException e) {
        log.warn("Violação de integridade", e);
        return resposta(HttpStatus.CONFLICT, "A operação conflita com dados já existentes");
    }

    @ExceptionHandler(SdkException.class)
    ResponseEntity<Erro> storage(SdkException e) {
        log.error("Falha ao acessar o armazenamento de relatórios", e);
        return resposta(HttpStatus.SERVICE_UNAVAILABLE, "O armazenamento de relatórios está indisponível");
    }

    private static ResponseEntity<Erro> resposta(HttpStatus status, String mensagem) {
        return ResponseEntity.status(status).body(new Erro(mensagem));
    }
}
