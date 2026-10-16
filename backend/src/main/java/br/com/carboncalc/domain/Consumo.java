package br.com.carboncalc.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

@Entity
public class Consumo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private FonteEmissao fonte;

    @Column(nullable = false, precision = 18, scale = 3)
    private BigDecimal quantidade;

    /** Mês de competência, guardado como o primeiro dia do mês. */
    @Column(nullable = false)
    private LocalDate competencia;

    protected Consumo() {
    }

    public Consumo(FonteEmissao fonte, BigDecimal quantidade, YearMonth competencia) {
        this.fonte = fonte;
        this.quantidade = quantidade;
        this.competencia = competencia.atDay(1);
    }

    public Long getId() {
        return id;
    }

    public FonteEmissao getFonte() {
        return fonte;
    }

    public BigDecimal getQuantidade() {
        return quantidade;
    }

    public YearMonth getCompetencia() {
        return YearMonth.from(competencia);
    }
}
