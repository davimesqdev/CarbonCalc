package br.com.carboncalc.domain;

public enum TipoAcaoCompensacao {
    ENERGIA_RENOVAVEL("Certificados de energia renovável (I-REC)"),
    CREDITOS_CARBONO("Compra de créditos de carbono"),
    REFLORESTAMENTO("Reflorestamento");

    private final String rotulo;

    TipoAcaoCompensacao(String rotulo) {
        this.rotulo = rotulo;
    }

    public String getRotulo() {
        return rotulo;
    }
}
