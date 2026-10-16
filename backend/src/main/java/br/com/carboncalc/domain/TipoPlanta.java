package br.com.carboncalc.domain;

public enum TipoPlanta {
    /** Planta ainda em projeto: os consumos são projeções de emissões futuras. */
    NOVA,
    /** Planta em operação: os consumos são o histórico realizado. */
    EXISTENTE
}
