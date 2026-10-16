package br.com.carboncalc.domain;

public enum TipoRelatorio {
    INVENTARIO_EMISSOES("Inventário de emissões"),
    PLANO_COMPENSACAO("Plano de compensação");

    private final String rotulo;

    TipoRelatorio(String rotulo) {
        this.rotulo = rotulo;
    }

    public String getRotulo() {
        return rotulo;
    }
}
