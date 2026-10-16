package br.com.carboncalc.domain;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("GESTOR")
public class GestorAmbiental extends Usuario {

    protected GestorAmbiental() {
    }

    GestorAmbiental(String nome, String email, String senhaHash) {
        super(nome, email, senhaHash);
    }

    @Override
    public Perfil getPerfil() {
        return Perfil.GESTOR;
    }
}
