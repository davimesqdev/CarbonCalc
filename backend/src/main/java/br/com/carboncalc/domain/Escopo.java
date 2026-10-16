package br.com.carboncalc.domain;

/** Escopos do inventário de gases de efeito estufa (GHG Protocol). */
public enum Escopo {
    /** Emissões diretas (combustão em fontes próprias). */
    ESCOPO_1,
    /** Emissões indiretas pela energia elétrica adquirida. */
    ESCOPO_2,
    /** Demais emissões indiretas da cadeia de valor. */
    ESCOPO_3
}
