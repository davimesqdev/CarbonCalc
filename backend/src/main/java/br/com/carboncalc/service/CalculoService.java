package br.com.carboncalc.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.YearMonth;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.carboncalc.domain.Calculo;
import br.com.carboncalc.domain.Consumo;
import br.com.carboncalc.domain.EmissoesPorEscopo;
import br.com.carboncalc.domain.Empresa;
import br.com.carboncalc.domain.FonteEmissao;
import br.com.carboncalc.domain.NaoEncontradoException;
import br.com.carboncalc.domain.Planta;
import br.com.carboncalc.domain.RegraNegocioException;
import br.com.carboncalc.domain.StatusPlanta;
import br.com.carboncalc.domain.TipoPlanta;
import br.com.carboncalc.dto.CalculoDtos.CalculoRequest;
import br.com.carboncalc.dto.CalculoDtos.CalculoResponse;
import br.com.carboncalc.dto.CalculoDtos.ComparacaoResponse;
import br.com.carboncalc.dto.CalculoDtos.ConsumoRequest;
import br.com.carboncalc.dto.CalculoDtos.ConsumoResponse;
import br.com.carboncalc.dto.CalculoDtos.Diferenca;
import br.com.carboncalc.repository.CalculoRepository;
import br.com.carboncalc.repository.ConsumoRepository;
import br.com.carboncalc.repository.EmpresaRepository;
import br.com.carboncalc.repository.FonteEmissaoRepository;

@Service
public class CalculoService {

    private static final String CENARIO_PADRAO = "Cenário base";

    private final EmpresaRepository empresas;
    private final FonteEmissaoRepository fontes;
    private final ConsumoRepository consumos;
    private final CalculoRepository calculos;
    private final FatorEmissaoService fatores;

    public CalculoService(EmpresaRepository empresas, FonteEmissaoRepository fontes, ConsumoRepository consumos,
            CalculoRepository calculos, FatorEmissaoService fatores) {
        this.empresas = empresas;
        this.fontes = fontes;
        this.consumos = consumos;
        this.calculos = calculos;
        this.fatores = fatores;
    }

    @Transactional
    public ConsumoResponse registrarConsumo(ConsumoRequest request) {
        FonteEmissao fonte = fontes.findById(request.fonteId())
                .orElseThrow(() -> new NaoEncontradoException("Fonte de emissão", request.fonteId()));
        Planta planta = fonte.getPlanta();
        if (planta.getStatus() != StatusPlanta.ATIVA) {
            throw new RegraNegocioException("Só é possível registrar consumo em plantas ativas; a planta "
                    + planta.getNome() + " está no estado " + planta.getStatus());
        }
        // Plantas existentes informam histórico; só plantas novas projetam emissões futuras.
        if (planta.getTipo() == TipoPlanta.EXISTENTE && request.competencia().isAfter(YearMonth.now())) {
            throw new RegraNegocioException(
                    "Plantas existentes registram apenas consumo já realizado; use uma planta nova para projeções");
        }
        return ConsumoResponse.de(consumos.save(new Consumo(fonte, request.quantidade(), request.competencia())));
    }

    @Transactional(readOnly = true)
    public List<ConsumoResponse> listarConsumos(Long empresaId) {
        return consumos.listarDaEmpresa(empresaId).stream().map(ConsumoResponse::de).toList();
    }

    @Transactional
    public CalculoResponse calcular(CalculoRequest request) {
        if (request.fim().isBefore(request.inicio())) {
            throw new RegraNegocioException("O fim do período não pode ser anterior ao início");
        }
        Empresa empresa = empresas.findById(request.empresaId())
                .orElseThrow(() -> new NaoEncontradoException("Empresa", request.empresaId()));
        List<EmissoesPorEscopo.Item> itens = consumos
                .buscarDoPeriodo(empresa.getId(), request.inicio().atDay(1), request.fim().atEndOfMonth())
                .stream()
                .map(c -> new EmissoesPorEscopo.Item(c.getFonte().getTipo(), c.getQuantidade()))
                .toList();
        if (itens.isEmpty()) {
            throw new RegraNegocioException(
                    "Dados insuficientes: não há consumo registrado para a empresa no período informado");
        }
        FatorEmissaoService.Fatores vigentes = fatores.obterFatores();
        EmissoesPorEscopo emissoes = EmissoesPorEscopo.calcular(itens, vigentes.valores());
        String cenario = request.cenario() == null || request.cenario().isBlank()
                ? CENARIO_PADRAO : request.cenario().trim();
        Calculo calculo = new Calculo(empresa, cenario, request.inicio(), request.fim(), emissoes,
                vigentes.origem());
        return CalculoResponse.de(calculos.save(calculo));
    }

    @Transactional(readOnly = true)
    public List<CalculoResponse> historico(Long empresaId) {
        return calculos.findByEmpresaIdOrderByDataCalculoDesc(empresaId).stream()
                .map(CalculoResponse::de)
                .toList();
    }

    @Transactional(readOnly = true)
    public CalculoResponse buscar(Long id) {
        return CalculoResponse.de(buscarCalculo(id));
    }

    @Transactional(readOnly = true)
    public ComparacaoResponse comparar(Long baseId, Long alternativoId) {
        Calculo base = buscarCalculo(baseId);
        Calculo alternativo = buscarCalculo(alternativoId);
        if (!base.getEmpresa().getId().equals(alternativo.getEmpresa().getId())) {
            throw new RegraNegocioException("Só é possível comparar cenários da mesma empresa");
        }
        List<Diferenca> diferencas = List.of(
                diferenca("Escopo 1", base.getEscopo1(), alternativo.getEscopo1()),
                diferenca("Escopo 2", base.getEscopo2(), alternativo.getEscopo2()),
                diferenca("Escopo 3", base.getEscopo3(), alternativo.getEscopo3()),
                diferenca("Total", base.getTotalCO2e(), alternativo.getTotalCO2e()));
        return new ComparacaoResponse(CalculoResponse.de(base), CalculoResponse.de(alternativo), diferencas);
    }

    private Calculo buscarCalculo(Long id) {
        return calculos.findById(id).orElseThrow(() -> new NaoEncontradoException("Cálculo", id));
    }

    private static Diferenca diferenca(String item, BigDecimal base, BigDecimal alternativo) {
        BigDecimal delta = alternativo.subtract(base);
        BigDecimal percentual = base.signum() == 0 ? null
                : delta.multiply(BigDecimal.valueOf(100)).divide(base, 1, RoundingMode.HALF_UP);
        return new Diferenca(item, base, alternativo, delta, percentual);
    }
}
