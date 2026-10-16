package br.com.carboncalc.integracao;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import br.com.carboncalc.domain.TipoFonte;

/**
 * Simula o "Sistema Externo de Dados (GHG Protocol)" dos diagramas. O GHG Protocol não oferece
 * uma API pública de fatores, então os valores de referência ficam aqui; para integrar uma fonte
 * real, basta outra implementação de {@link FonteFatoresExterna}.
 */
@Component
public class GhgProtocolSimulado implements FonteFatoresExterna {

    private final boolean disponivel;

    public GhgProtocolSimulado(@Value("${carboncalc.ghg.disponivel}") boolean disponivel) {
        this.disponivel = disponivel;
    }

    @Override
    public Map<TipoFonte, BigDecimal> buscarFatores() {
        if (!disponivel) {
            throw new IllegalStateException("Fonte externa de fatores indisponível");
        }
        Map<TipoFonte, BigDecimal> fatores = new EnumMap<>(TipoFonte.class);
        fatores.put(TipoFonte.DIESEL, new BigDecimal("2.603"));
        fatores.put(TipoFonte.GASOLINA, new BigDecimal("2.212"));
        fatores.put(TipoFonte.GAS_NATURAL, new BigDecimal("2.066"));
        fatores.put(TipoFonte.GLP, new BigDecimal("2.932"));
        fatores.put(TipoFonte.ELETRICIDADE, new BigDecimal("0.0545"));
        fatores.put(TipoFonte.VIAGEM_AEREA, new BigDecimal("0.110"));
        fatores.put(TipoFonte.TRANSPORTE_TERCEIRIZADO, new BigDecimal("0.100"));
        fatores.put(TipoFonte.RESIDUOS_ATERRO, new BigDecimal("0.450"));
        return fatores;
    }
}
