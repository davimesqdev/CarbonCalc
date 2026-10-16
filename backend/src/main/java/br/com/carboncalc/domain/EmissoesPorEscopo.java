package br.com.carboncalc.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/** Emissões em toneladas de CO2e, separadas por escopo. */
public record EmissoesPorEscopo(BigDecimal escopo1, BigDecimal escopo2, BigDecimal escopo3) {

    private static final BigDecimal KG_POR_TONELADA = BigDecimal.valueOf(1000);

    public record Item(TipoFonte tipo, BigDecimal quantidade) {
    }

    /**
     * Converte consumos em emissões: quantidade x fator (kg CO2e por unidade), somado por escopo
     * e expresso em toneladas com três casas decimais.
     */
    public static EmissoesPorEscopo calcular(List<Item> consumos, Map<TipoFonte, BigDecimal> fatores) {
        Map<Escopo, BigDecimal> kgPorEscopo = new EnumMap<>(Escopo.class);
        for (Item item : consumos) {
            BigDecimal fator = fatores.get(item.tipo());
            if (fator == null) {
                throw new RegraNegocioException(
                        "Não há fator de emissão vigente para " + item.tipo().getRotulo());
            }
            kgPorEscopo.merge(item.tipo().getEscopo(), item.quantidade().multiply(fator), BigDecimal::add);
        }
        return new EmissoesPorEscopo(
                emToneladas(kgPorEscopo.get(Escopo.ESCOPO_1)),
                emToneladas(kgPorEscopo.get(Escopo.ESCOPO_2)),
                emToneladas(kgPorEscopo.get(Escopo.ESCOPO_3)));
    }

    public BigDecimal total() {
        return escopo1.add(escopo2).add(escopo3);
    }

    private static BigDecimal emToneladas(BigDecimal kg) {
        BigDecimal valor = kg == null ? BigDecimal.ZERO : kg;
        return valor.divide(KG_POR_TONELADA, 3, RoundingMode.HALF_UP);
    }
}
