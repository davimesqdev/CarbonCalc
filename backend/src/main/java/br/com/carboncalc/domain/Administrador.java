package br.com.carboncalc.domain;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("ADMINISTRADOR")
public class Administrador extends Usuario {

    protected Administrador() {
    }

    Administrador(String nome, String email, String senhaHash) {
        super(nome, email, senhaHash);
    }

    @Override
    public Perfil getPerfil() {
        return Perfil.ADMINISTRADOR;
    }
}
