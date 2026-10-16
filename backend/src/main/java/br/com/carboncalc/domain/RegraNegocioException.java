package br.com.carboncalc.domain;

/** Violação de uma regra de negócio; a API responde com 422. */
public class RegraNegocioException extends RuntimeException {

    public RegraNegocioException(String mensagem) {
        super(mensagem);
    }
}
