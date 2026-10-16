package br.com.carboncalc.domain;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

/** Fator de emissão de um tipo de fonte, em kg CO2e por unidade consumida. */
@Entity
public class FatorEmissao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoFonte tipoFonte;

    @Column(nullable = false, precision = 12, scale = 6)
    private BigDecimal valor;

    @Column(nullable = false)
    private String unidade;

    @Column(nullable = false)
    private LocalDate vigenciaInicio;

    private LocalDate vigenciaFim;

    protected FatorEmissao() {
    }

    public FatorEmissao(TipoFonte tipoFonte, BigDecimal valor, LocalDate vigenciaInicio) {
        this.tipoFonte = tipoFonte;
        this.valor = valor;
        this.unidade = tipoFonte.getUnidade();
        this.vigenciaInicio = vigenciaInicio;
    }

    /** Encerra a vigência deste fator e devolve o que passa a valer a partir de {@code data}. */
    public FatorEmissao atualizar(BigDecimal novoValor, LocalDate data) {
        this.vigenciaFim = data;
        return new FatorEmissao(tipoFonte, novoValor, data);
    }

    public Long getId() {
        return id;
    }

    public TipoFonte getTipoFonte() {
        return tipoFonte;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public String getUnidade() {
        return unidade;
    }

    public LocalDate getVigenciaInicio() {
        return vigenciaInicio;
    }

    public LocalDate getVigenciaFim() {
        return vigenciaFim;
    }
}
