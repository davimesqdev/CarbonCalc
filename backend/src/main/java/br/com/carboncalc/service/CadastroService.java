package br.com.carboncalc.service;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.carboncalc.domain.AcaoPlanta;
import br.com.carboncalc.domain.Empresa;
import br.com.carboncalc.domain.FonteEmissao;
import br.com.carboncalc.domain.NaoEncontradoException;
import br.com.carboncalc.domain.Planta;
import br.com.carboncalc.domain.RegraNegocioException;
import br.com.carboncalc.domain.StatusPlanta;
import br.com.carboncalc.dto.CadastroDtos.EmpresaRequest;
import br.com.carboncalc.dto.CadastroDtos.EmpresaResponse;
import br.com.carboncalc.dto.CadastroDtos.FonteRequest;
import br.com.carboncalc.dto.CadastroDtos.FonteResponse;
import br.com.carboncalc.dto.CadastroDtos.PlantaRequest;
import br.com.carboncalc.dto.CadastroDtos.PlantaResponse;
import br.com.carboncalc.dto.CadastroDtos.TransicaoRequest;
import br.com.carboncalc.repository.ConsumoRepository;
import br.com.carboncalc.repository.EmpresaRepository;
import br.com.carboncalc.repository.FonteEmissaoRepository;
import br.com.carboncalc.repository.PlantaRepository;

@Service
public class CadastroService {

    /** Uma planta ativa sem consumo registrado nesta janela é considerada ociosa. */
    private static final int MESES_ATE_OCIOSA = 3;

    private static final Logger log = LoggerFactory.getLogger(CadastroService.class);

    private final EmpresaRepository empresas;
    private final PlantaRepository plantas;
    private final FonteEmissaoRepository fontes;
    private final ConsumoRepository consumos;

    public CadastroService(EmpresaRepository empresas, PlantaRepository plantas, FonteEmissaoRepository fontes,
            ConsumoRepository consumos) {
        this.empresas = empresas;
        this.plantas = plantas;
        this.fontes = fontes;
        this.consumos = consumos;
    }

    @Transactional(readOnly = true)
    public List<EmpresaResponse> listarEmpresas() {
        return empresas.findAll(Sort.by("nome")).stream().map(EmpresaResponse::de).toList();
    }

    @Transactional
    public EmpresaResponse cadastrarEmpresa(EmpresaRequest request) {
        if (empresas.existsByCnpj(request.cnpj())) {
            throw new RegraNegocioException("Já existe uma empresa com o CNPJ " + request.cnpj());
        }
        return EmpresaResponse.de(empresas.save(new Empresa(request.nome(), request.cnpj(), request.setor())));
    }

    @Transactional(readOnly = true)
    public List<PlantaResponse> listarPlantas(Long empresaId) {
        return plantas.findByEmpresaIdOrderByNome(empresaId).stream().map(this::paraResposta).toList();
    }

    @Transactional
    public PlantaResponse cadastrarPlanta(Long empresaId, PlantaRequest request) {
        Empresa empresa = empresas.findById(empresaId)
                .orElseThrow(() -> new NaoEncontradoException("Empresa", empresaId));
        Planta planta = new Planta(empresa, request.nome(), request.localizacao(), request.tipo());
        return paraResposta(plantas.save(planta));
    }

    @Transactional
    public PlantaResponse transicionarPlanta(Long plantaId, TransicaoRequest request) {
        Planta planta = buscarPlanta(plantaId);
        if (request.acao() == AcaoPlanta.ATIVAR && Boolean.TRUE.equals(request.documentacaoOk())) {
            planta.confirmarDocumentacao();
        }
        planta.aplicar(request.acao());
        return paraResposta(planta);
    }

    @Transactional(readOnly = true)
    public List<FonteResponse> listarFontes(Long plantaId) {
        return fontes.findByPlantaIdOrderByDescricao(plantaId).stream().map(FonteResponse::de).toList();
    }

    @Transactional
    public FonteResponse registrarFonte(Long plantaId, FonteRequest request) {
        Planta planta = buscarPlanta(plantaId);
        if (planta.getStatus() == StatusPlanta.ARQUIVADA) {
            throw new RegraNegocioException("Plantas arquivadas não recebem novas fontes de emissão");
        }
        return FonteResponse.de(fontes.save(new FonteEmissao(planta, request.tipo(), request.descricao())));
    }

    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void arquivarPlantasInativasAntigas() {
        LocalDateTime limite = LocalDateTime.now().minusYears(Planta.ANOS_ATE_ARQUIVAR);
        List<Planta> vencidas = plantas.findByStatusAndInativadaEmBefore(StatusPlanta.INATIVA, limite);
        vencidas.forEach(Planta::arquivarAutomaticamente);
        if (!vencidas.isEmpty()) {
            log.info("{} planta(s) inativa(s) arquivada(s) automaticamente", vencidas.size());
        }
    }

    private Planta buscarPlanta(Long plantaId) {
        return plantas.findById(plantaId).orElseThrow(() -> new NaoEncontradoException("Planta", plantaId));
    }

    private PlantaResponse paraResposta(Planta planta) {
        String subestado = null;
        if (planta.getStatus() == StatusPlanta.ATIVA) {
            var desde = YearMonth.now().minusMonths(MESES_ATE_OCIOSA - 1).atDay(1);
            boolean temConsumoRecente = planta.getId() != null
                    && consumos.existsByFontePlantaIdAndCompetenciaGreaterThanEqual(planta.getId(), desde);
            subestado = temConsumoRecente ? "OPERANDO" : "OCIOSA";
        }
        return PlantaResponse.de(planta, subestado);
    }
}
