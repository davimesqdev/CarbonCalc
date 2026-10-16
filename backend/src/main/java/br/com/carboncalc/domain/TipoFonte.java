package br.com.carboncalc.domain;

public enum TipoFonte {
    DIESEL("Diesel", Escopo.ESCOPO_1, "L"),
    GASOLINA("Gasolina", Escopo.ESCOPO_1, "L"),
    GAS_NATURAL("Gás natural", Escopo.ESCOPO_1, "m³"),
    GLP("GLP", Escopo.ESCOPO_1, "kg"),
    ELETRICIDADE("Energia elétrica", Escopo.ESCOPO_2, "kWh"),
    VIAGEM_AEREA("Viagens aéreas", Escopo.ESCOPO_3, "passageiro.km"),
    TRANSPORTE_TERCEIRIZADO("Transporte terceirizado", Escopo.ESCOPO_3, "t.km"),
    RESIDUOS_ATERRO("Resíduos em aterro", Escopo.ESCOPO_3, "kg");

    private final String rotulo;
    private final Escopo escopo;
    private final String unidade;

    TipoFonte(String rotulo, Escopo escopo, String unidade) {
        this.rotulo = rotulo;
        this.escopo = escopo;
        this.unidade = unidade;
    }

    public String getRotulo() {
        return rotulo;
    }

    public Escopo getEscopo() {
        return escopo;
    }

    public String getUnidade() {
        return unidade;
    }
}
