package br.com.carboncalc.domain;

import java.time.LocalDateTime;

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
public class Relatorio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Empresa empresa;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Calculo calculo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoRelatorio tipo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Formato formato;

    @Column(nullable = false)
    private LocalDateTime geradoEm;

    @Column(nullable = false)
    private String chaveStorage;

    private String protocoloEnvio;

    private LocalDateTime enviadoEm;

    protected Relatorio() {
    }

    public Relatorio(Calculo calculo, TipoRelatorio tipo, Formato formato, String chaveStorage) {
        this.empresa = calculo.getEmpresa();
        this.calculo = calculo;
        this.tipo = tipo;
        this.formato = formato;
        this.chaveStorage = chaveStorage;
        this.geradoEm = LocalDateTime.now();
    }

    public void registrarEnvio(String protocolo) {
        if (protocoloEnvio != null) {
            throw new RegraNegocioException("Relatório já enviado ao órgão sob o protocolo " + protocoloEnvio);
        }
        this.protocoloEnvio = protocolo;
        this.enviadoEm = LocalDateTime.now();
    }

    public String nomeArquivo() {
        return "carboncalc-" + tipo.name().toLowerCase().replace('_', '-') + "-" + id + "." + formato.getExtensao();
    }

    public Long getId() {
        return id;
    }

    public Empresa getEmpresa() {
        return empresa;
    }

    public Calculo getCalculo() {
        return calculo;
    }

    public TipoRelatorio getTipo() {
        return tipo;
    }

    public Formato getFormato() {
        return formato;
    }

    public LocalDateTime getGeradoEm() {
        return geradoEm;
    }

    public String getChaveStorage() {
        return chaveStorage;
    }

    public String getProtocoloEnvio() {
        return protocoloEnvio;
    }

    public LocalDateTime getEnviadoEm() {
        return enviadoEm;
    }
}
