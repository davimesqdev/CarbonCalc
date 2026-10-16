package br.com.carboncalc.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import br.com.carboncalc.domain.EmissoesPorEscopo.Item;

class EmissoesPorEscopoTest {

    private static final Map<TipoFonte, BigDecimal> FATORES = Map.of(
            TipoFonte.DIESEL, new BigDecimal("2.603"),
            TipoFonte.GLP, new BigDecimal("2.932"),
            TipoFonte.ELETRICIDADE, new BigDecimal("0.0545"),
            TipoFonte.VIAGEM_AEREA, new BigDecimal("0.110"));

    @Test
    void somaPorEscopoEConverteParaToneladas() {
        EmissoesPorEscopo emissoes = EmissoesPorEscopo.calcular(List.of(
                new Item(TipoFonte.DIESEL, new BigDecimal("10000")),
                new Item(TipoFonte.GLP, new BigDecimal("500")),
                new Item(TipoFonte.ELETRICIDADE, new BigDecimal("200000")),
                new Item(TipoFonte.VIAGEM_AEREA, new BigDecimal("30000"))), FATORES);

        // 10000 L x 2,603 + 500 kg x 2,932 = 27 496 kg
        assertThat(emissoes.escopo1()).isEqualByComparingTo("27.496");
        // 200000 kWh x 0,0545 = 10 900 kg
        assertThat(emissoes.escopo2()).isEqualByComparingTo("10.900");
        // 30000 passageiro.km x 0,110 = 3 300 kg
        assertThat(emissoes.escopo3()).isEqualByComparingTo("3.300");
        assertThat(emissoes.total()).isEqualByComparingTo("41.696");
    }

    @Test
    void escopoSemConsumoFicaZerado() {
        EmissoesPorEscopo emissoes = EmissoesPorEscopo.calcular(
                List.of(new Item(TipoFonte.ELETRICIDADE, new BigDecimal("1000"))), FATORES);

        assertThat(emissoes.escopo1()).isEqualByComparingTo("0");
        assertThat(emissoes.escopo2()).isEqualByComparingTo("0.055");
        assertThat(emissoes.escopo3()).isEqualByComparingTo("0");
    }

    @Test
    void recusaConsumoSemFatorVigente() {
        List<Item> consumos = List.of(new Item(TipoFonte.GASOLINA, BigDecimal.TEN));

        assertThatThrownBy(() -> EmissoesPorEscopo.calcular(consumos, FATORES))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("Gasolina");
    }
}
