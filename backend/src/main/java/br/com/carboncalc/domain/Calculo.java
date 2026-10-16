package br.com.carboncalc.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

/** Resultado de um cálculo de pegada de carbono, em toneladas de CO2e. */
@Entity
public class Calculo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Empresa empresa;

    @Column(nullable = false)
    private String cenario;

    @Column(nullable = false)
    private LocalDate periodoInicio;

    @Column(nullable = false)
    private LocalDate periodoFim;

    @Column(nullable = false, precision = 18, scale = 3)
    private BigDecimal escopo1;

    @Column(nullable = false, precision = 18, scale = 3)
    private BigDecimal escopo2;

    @Column(nullable = false, precision = 18, scale = 3)
    private BigDecimal escopo3;

    @Column(name = "total_co2e", nullable = false, precision = 18, scale = 3)
    private BigDecimal totalCO2e;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrigemFatores origemFatores;

    @Column(nullable = false)
    private LocalDateTime dataCalculo;

    protected Calculo() {
    }

    public Calculo(Empresa empresa, String cenario, YearMonth inicio, YearMonth fim,
            EmissoesPorEscopo emissoes, OrigemFatores origemFatores) {
        this.empresa = empresa;
        this.cenario = cenario;
        this.periodoInicio = inicio.atDay(1);
        this.periodoFim = fim.atEndOfMonth();
        this.escopo1 = emissoes.escopo1();
        this.escopo2 = emissoes.escopo2();
        this.escopo3 = emissoes.escopo3();
        this.totalCO2e = emissoes.total();
        this.origemFatores = origemFatores;
        this.dataCalculo = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Empresa getEmpresa() {
        return empresa;
    }

    public String getCenario() {
        return cenario;
    }

    public LocalDate getPeriodoInicio() {
        return periodoInicio;
    }

    public LocalDate getPeriodoFim() {
        return periodoFim;
    }

    public BigDecimal getEscopo1() {
        return escopo1;
    }

    public BigDecimal getEscopo2() {
        return escopo2;
    }

    public BigDecimal getEscopo3() {
        return escopo3;
    }

    public BigDecimal getTotalCO2e() {
        return totalCO2e;
    }

    public OrigemFatores getOrigemFatores() {
        return origemFatores;
    }

    public LocalDateTime getDataCalculo() {
        return dataCalculo;
    }
}
