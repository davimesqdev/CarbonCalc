package br.com.carboncalc.integracao;

import java.math.BigDecimal;
import java.util.Map;

import br.com.carboncalc.domain.TipoFonte;

/** Fonte externa de fatores de emissão, em kg CO2e por unidade. Pode falhar ou demorar. */
public interface FonteFatoresExterna {

    Map<TipoFonte, BigDecimal> buscarFatores();
}
