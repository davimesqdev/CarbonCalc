package br.com.carboncalc.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;

import br.com.carboncalc.domain.Calculo;
import br.com.carboncalc.domain.Compensacao;
import br.com.carboncalc.domain.Consumo;
import br.com.carboncalc.domain.FonteEmissao;
import br.com.carboncalc.domain.OrigemFatores;
import br.com.carboncalc.domain.TipoAcaoCompensacao;
import br.com.carboncalc.domain.TipoFonte;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public final class CalculoDtos {

    private CalculoDtos() {
    }

    public record ConsumoRequest(@NotNull Long fonteId, @NotNull YearMonth competencia,
            @NotNull @Positive BigDecimal quantidade) {
    }

    public record ConsumoResponse(Long id, Long fonteId, String fonte, TipoFonte tipo, String unidade,
            String planta, String competencia, BigDecimal quantidade) {

        public static ConsumoResponse de(Consumo consumo) {
            FonteEmissao fonte = consumo.getFonte();
            return new ConsumoResponse(consumo.getId(), fonte.getId(), fonte.getDescricao(), fonte.getTipo(),
                    fonte.getTipo().getUnidade(), fonte.getPlanta().getNome(),
                    consumo.getCompetencia().toString(), consumo.getQuantidade());
        }
    }

    public record CalculoRequest(@NotNull Long empresaId, @NotNull YearMonth inicio, @NotNull YearMonth fim,
            @Size(max = 120) String cenario) {
    }

    /** Emissões em toneladas de CO2e. */
    public record CalculoResponse(Long id, Long empresaId, String cenario, LocalDate periodoInicio,
            LocalDate periodoFim, BigDecimal escopo1, BigDecimal escopo2, BigDecimal escopo3,
            BigDecimal totalCO2e, OrigemFatores origemFatores, LocalDateTime dataCalculo) {

        public static CalculoResponse de(Calculo calculo) {
            return new CalculoResponse(calculo.getId(), calculo.getEmpresa().getId(), calculo.getCenario(),
                    calculo.getPeriodoInicio(), calculo.getPeriodoFim(), calculo.getEscopo1(),
                    calculo.getEscopo2(), calculo.getEscopo3(), calculo.getTotalCO2e(),
                    calculo.getOrigemFatores(), calculo.getDataCalculo());
        }
    }

    /** {@code percentual} é nulo quando o valor base é zero. */
    public record Diferenca(String item, BigDecimal base, BigDecimal alternativo, BigDecimal diferenca,
            BigDecimal percentual) {
    }

    public record ComparacaoResponse(CalculoResponse base, CalculoResponse alternativo,
            List<Diferenca> diferencas) {
    }

    public record CompensacaoRequest(
            @NotNull @DecimalMin("1") @DecimalMax("100") BigDecimal percentual) {
    }

    public record CompensacaoResponse(Long id, TipoAcaoCompensacao tipoAcao, String rotulo, String descricao,
            BigDecimal creditosNecessarios, BigDecimal quantidade, String unidade, BigDecimal custoEstimado) {

        public static CompensacaoResponse de(Compensacao compensacao) {
            return new CompensacaoResponse(compensacao.getId(), compensacao.getTipoAcao(),
                    compensacao.getTipoAcao().getRotulo(), compensacao.getDescricao(),
                    compensacao.getCreditosNecessarios(), compensacao.getQuantidade(),
                    compensacao.getUnidade(), compensacao.getCustoEstimado());
        }
    }

    public record PlanoCompensacaoResponse(Long calculoId, BigDecimal totalCO2e, BigDecimal creditosNecessarios,
            BigDecimal custoTotal, List<CompensacaoResponse> acoes) {
    }
}
