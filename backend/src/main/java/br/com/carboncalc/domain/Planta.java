package br.com.carboncalc.domain;

import java.time.LocalDateTime;
import java.util.List;

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
public class Planta {

    /** Anos em que uma planta inativa permanece consultável antes do arquivamento automático. */
    public static final int ANOS_ATE_ARQUIVAR = 5;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Empresa empresa;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false)
    private String localizacao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoPlanta tipo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusPlanta status = StatusPlanta.CADASTRADA;

    @Column(nullable = false)
    private boolean documentacaoOk;

    private LocalDateTime inativadaEm;

    protected Planta() {
    }

    public Planta(Empresa empresa, String nome, String localizacao, TipoPlanta tipo) {
        this.empresa = empresa;
        this.nome = nome;
        this.localizacao = localizacao;
        this.tipo = tipo;
    }

    /** Aplica uma transição do diagrama de estados, recusando as que não partem do estado atual. */
    public void aplicar(AcaoPlanta acao) {
        if (!acoesDisponiveis().contains(acao)) {
            throw new RegraNegocioException(
                    "Ação " + acao + " não é permitida para uma planta no estado " + status);
        }
        switch (acao) {
            case ATIVAR -> {
                if (!documentacaoOk) {
                    throw new RegraNegocioException("A planta só pode ser ativada com a documentação conferida");
                }
                status = StatusPlanta.ATIVA;
            }
            case CANCELAR_CADASTRO -> status = StatusPlanta.ARQUIVADA;
            case ENTRAR_MANUTENCAO -> status = StatusPlanta.EM_MANUTENCAO;
            case SAIR_MANUTENCAO -> status = StatusPlanta.ATIVA;
            case INATIVAR -> {
                status = StatusPlanta.INATIVA;
                inativadaEm = LocalDateTime.now();
            }
            case REATIVAR -> {
                status = StatusPlanta.ATIVA;
                inativadaEm = null;
            }
        }
    }

    public List<AcaoPlanta> acoesDisponiveis() {
        return switch (status) {
            case CADASTRADA -> List.of(AcaoPlanta.ATIVAR, AcaoPlanta.CANCELAR_CADASTRO);
            case ATIVA -> List.of(AcaoPlanta.ENTRAR_MANUTENCAO, AcaoPlanta.INATIVAR);
            case EM_MANUTENCAO -> List.of(AcaoPlanta.SAIR_MANUTENCAO);
            case INATIVA -> List.of(AcaoPlanta.REATIVAR);
            case ARQUIVADA -> List.of();
        };
    }

    /** Transição temporal do diagrama de estados: inativa há mais de cinco anos. */
    public void arquivarAutomaticamente() {
        if (status != StatusPlanta.INATIVA) {
            throw new RegraNegocioException("Apenas plantas inativas são arquivadas automaticamente");
        }
        status = StatusPlanta.ARQUIVADA;
    }

    public void confirmarDocumentacao() {
        this.documentacaoOk = true;
    }

    public Long getId() {
        return id;
    }

    public Empresa getEmpresa() {
        return empresa;
    }

    public String getNome() {
        return nome;
    }

    public String getLocalizacao() {
        return localizacao;
    }

    public TipoPlanta getTipo() {
        return tipo;
    }

    public StatusPlanta getStatus() {
        return status;
    }

    public boolean isDocumentacaoOk() {
        return documentacaoOk;
    }

    public LocalDateTime getInativadaEm() {
        return inativadaEm;
    }
}
