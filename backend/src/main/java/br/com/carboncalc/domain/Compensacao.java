package br.com.carboncalc.domain;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

/** Ação sugerida para compensar parte das emissões de um cálculo. Um crédito equivale a 1 t CO2e. */
@Entity
public class Compensacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Calculo calculo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoAcaoCompensacao tipoAcao;

    @Column(nullable = false, length = 300)
    private String descricao;

    @Column(nullable = false, precision = 18, scale = 3)
    private BigDecimal creditosNecessarios;

    @Column(nullable = false, precision = 18, scale = 3)
    private BigDecimal quantidade;

    @Column(nullable = false)
    private String unidade;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal custoEstimado;

    protected Compensacao() {
    }

    public Compensacao(Calculo calculo, TipoAcaoCompensacao tipoAcao, String descricao,
            BigDecimal creditosNecessarios, BigDecimal quantidade, String unidade, BigDecimal custoEstimado) {
        this.calculo = calculo;
        this.tipoAcao = tipoAcao;
        this.descricao = descricao;
        this.creditosNecessarios = creditosNecessarios;
        this.quantidade = quantidade;
        this.unidade = unidade;
        this.custoEstimado = custoEstimado;
    }

    public Long getId() {
        return id;
    }

    public Calculo getCalculo() {
        return calculo;
    }

    public TipoAcaoCompensacao getTipoAcao() {
        return tipoAcao;
    }

    public String getDescricao() {
        return descricao;
    }

    public BigDecimal getCreditosNecessarios() {
        return creditosNecessarios;
    }

    public BigDecimal getQuantidade() {
        return quantidade;
    }

    public String getUnidade() {
        return unidade;
    }

    public BigDecimal getCustoEstimado() {
        return custoEstimado;
    }
}
