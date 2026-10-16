package br.com.carboncalc.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

@Entity
public class FonteEmissao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Planta planta;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoFonte tipo;

    @Column(nullable = false)
    private String descricao;

    protected FonteEmissao() {
    }

    public FonteEmissao(Planta planta, TipoFonte tipo, String descricao) {
        this.planta = planta;
        this.tipo = tipo;
        this.descricao = descricao;
    }

    public Long getId() {
        return id;
    }

    public Planta getPlanta() {
        return planta;
    }

    public TipoFonte getTipo() {
        return tipo;
    }

    public String getDescricao() {
        return descricao;
    }
}
