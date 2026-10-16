package br.com.carboncalc.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.carboncalc.domain.Calculo;
import br.com.carboncalc.domain.Compensacao;
import br.com.carboncalc.domain.NaoEncontradoException;
import br.com.carboncalc.domain.RegraNegocioException;
import br.com.carboncalc.domain.TipoAcaoCompensacao;
import br.com.carboncalc.domain.TipoFonte;
import br.com.carboncalc.dto.CalculoDtos.CompensacaoResponse;
import br.com.carboncalc.dto.CalculoDtos.PlanoCompensacaoResponse;
import br.com.carboncalc.repository.CalculoRepository;
import br.com.carboncalc.repository.CompensacaoRepository;
import br.com.carboncalc.repository.FatorEmissaoRepository;

/**
 * Calcula quanto precisa ser compensado e sugere como. Um crédito equivale a 1 t CO2e.
 * Os preços e rendimentos abaixo são premissas ilustrativas, não cotações de mercado.
 */
@Service
public class CompensacaoService {

    private static final BigDecimal PRECO_CREDITO = new BigDecimal("60.00");
    private static final BigDecimal PRECO_IREC = new BigDecimal("5.00");
    private static final BigDecimal PRECO_MUDA = new BigDecimal("25.00");
    /** Mudas necessárias para absorver 1 t CO2e ao longo de 20 anos. */
    private static final BigDecimal MUDAS_POR_TONELADA = new BigDecimal("7");
    /** Parcela das emissões fora do Escopo 2 coberta por créditos; o restante vai para reflorestamento. */
    private static final BigDecimal PARCELA_CREDITOS = new BigDecimal("0.60");

    private final CalculoRepository calculos;
    private final CompensacaoRepository compensacoes;
    private final FatorEmissaoRepository fatores;

    public CompensacaoService(CalculoRepository calculos, CompensacaoRepository compensacoes,
            FatorEmissaoRepository fatores) {
        this.calculos = calculos;
        this.compensacoes = compensacoes;
        this.fatores = fatores;
    }

    /**
     * Gera o plano para compensar {@code percentual}% das emissões do cálculo, substituindo o plano
     * anterior. A parte do Escopo 2 é coberta por certificados de energia renovável; o restante é
     * dividido entre créditos de carbono e reflorestamento.
     */
    @Transactional
    public PlanoCompensacaoResponse gerarPlano(Long calculoId, BigDecimal percentual) {
        Calculo calculo = buscarCalculo(calculoId);
        BigDecimal fracao = percentual.movePointLeft(2);
        BigDecimal alvoTotal = calculo.getTotalCO2e().multiply(fracao).setScale(3, RoundingMode.HALF_UP);
        BigDecimal alvoEscopo2 = calculo.getEscopo2().multiply(fracao).setScale(3, RoundingMode.HALF_UP);
        BigDecimal restante = alvoTotal.subtract(alvoEscopo2);
        BigDecimal emCreditos = restante.multiply(PARCELA_CREDITOS).setScale(3, RoundingMode.HALF_UP);
        BigDecimal emReflorestamento = restante.subtract(emCreditos);

        List<Compensacao> plano = new ArrayList<>();
        if (alvoEscopo2.signum() > 0) {
            // O fator da eletricidade em kg CO2e/kWh equivale a t CO2e/MWh, e 1 I-REC certifica 1 MWh.
            BigDecimal fatorEletricidade = fatores.findByTipoFonteAndVigenciaFimIsNull(TipoFonte.ELETRICIDADE)
                    .orElseThrow(() -> new RegraNegocioException("Não há fator vigente para energia elétrica"))
                    .getValor();
            BigDecimal certificados = alvoEscopo2.divide(fatorEletricidade, 0, RoundingMode.UP);
            plano.add(new Compensacao(calculo, TipoAcaoCompensacao.ENERGIA_RENOVAVEL,
                    "Neutraliza as emissões da energia elétrica (Escopo 2) com certificados de origem renovável.",
                    alvoEscopo2, certificados, "I-RECs (MWh)", certificados.multiply(PRECO_IREC)));
        }
        if (emCreditos.signum() > 0) {
            BigDecimal creditos = emCreditos.setScale(0, RoundingMode.UP);
            plano.add(new Compensacao(calculo, TipoAcaoCompensacao.CREDITOS_CARBONO,
                    "Aquisição de créditos de carbono certificados no mercado voluntário.",
                    emCreditos, creditos, "créditos", creditos.multiply(PRECO_CREDITO)));
        }
        if (emReflorestamento.signum() > 0) {
            BigDecimal mudas = emReflorestamento.multiply(MUDAS_POR_TONELADA).setScale(0, RoundingMode.UP);
            plano.add(new Compensacao(calculo, TipoAcaoCompensacao.REFLORESTAMENTO,
                    "Plantio de mudas nativas, com absorção estimada ao longo de 20 anos.",
                    emReflorestamento, mudas, "mudas", mudas.multiply(PRECO_MUDA)));
        }

        compensacoes.deleteAll(compensacoes.findByCalculoIdOrderById(calculoId));
        return montarPlano(calculo, compensacoes.saveAll(plano));
    }

    @Transactional(readOnly = true)
    public PlanoCompensacaoResponse buscarPlano(Long calculoId) {
        return montarPlano(buscarCalculo(calculoId), compensacoes.findByCalculoIdOrderById(calculoId));
    }

    private Calculo buscarCalculo(Long id) {
        return calculos.findById(id).orElseThrow(() -> new NaoEncontradoException("Cálculo", id));
    }

    private static PlanoCompensacaoResponse montarPlano(Calculo calculo, List<Compensacao> acoes) {
        BigDecimal creditos = acoes.stream().map(Compensacao::getCreditosNecessarios)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal custo = acoes.stream().map(Compensacao::getCustoEstimado)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new PlanoCompensacaoResponse(calculo.getId(), calculo.getTotalCO2e(), creditos, custo,
                acoes.stream().map(CompensacaoResponse::de).toList());
    }
}
