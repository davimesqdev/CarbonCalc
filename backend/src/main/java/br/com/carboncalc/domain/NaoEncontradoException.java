package br.com.carboncalc.domain;

public class NaoEncontradoException extends RuntimeException {

    public NaoEncontradoException(String recurso, Long id) {
        super(recurso + " " + id + " não encontrado(a)");
    }
}
