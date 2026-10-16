package br.com.carboncalc.service;

import java.time.Year;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.carboncalc.domain.Calculo;
import br.com.carboncalc.domain.NaoEncontradoException;
import br.com.carboncalc.domain.Relatorio;
import br.com.carboncalc.dto.RelatorioDtos.Arquivo;
import br.com.carboncalc.dto.RelatorioDtos.RelatorioRequest;
import br.com.carboncalc.dto.RelatorioDtos.RelatorioResponse;
import br.com.carboncalc.integracao.StorageService;
import br.com.carboncalc.repository.CalculoRepository;
import br.com.carboncalc.repository.CompensacaoRepository;
import br.com.carboncalc.repository.RelatorioRepository;

@Service
public class RelatorioService {

    private final RelatorioRepository relatorios;
    private final CalculoRepository calculos;
    private final CompensacaoRepository compensacoes;
    private final GeradorRelatorio gerador;
    private final StorageService storage;

    public RelatorioService(RelatorioRepository relatorios, CalculoRepository calculos,
            CompensacaoRepository compensacoes, GeradorRelatorio gerador, StorageService storage) {
        this.relatorios = relatorios;
        this.calculos = calculos;
        this.compensacoes = compensacoes;
        this.gerador = gerador;
        this.storage = storage;
    }

    @Transactional
    public RelatorioResponse gerar(RelatorioRequest request) {
        Calculo calculo = calculos.findById(request.calculoId())
                .orElseThrow(() -> new NaoEncontradoException("Cálculo", request.calculoId()));
        byte[] conteudo = gerador.gerar(request.tipo(), request.formato(), calculo,
                compensacoes.findByCalculoIdOrderById(calculo.getId()));
        String chave = "empresa-" + calculo.getEmpresa().getId() + "/calculo-" + calculo.getId() + "/"
                + UUID.randomUUID() + "." + request.formato().getExtensao();
        storage.salvar(chave, conteudo, request.formato().getContentType());
        return RelatorioResponse.de(relatorios.save(new Relatorio(calculo, request.tipo(), request.formato(), chave)));
    }

    @Transactional(readOnly = true)
    public List<RelatorioResponse> listar(Long empresaId) {
        return relatorios.findByEmpresaIdOrderByGeradoEmDesc(empresaId).stream()
                .map(RelatorioResponse::de)
                .toList();
    }

    @Transactional(readOnly = true)
    public Arquivo baixar(Long id) {
        Relatorio relatorio = buscar(id);
        return new Arquivo(relatorio.nomeArquivo(), relatorio.getFormato().getContentType(),
                storage.ler(relatorio.getChaveStorage()));
    }

    /**
     * Registra o envio ao órgão regulador e devolve o protocolo. O envio é simulado: não há
     * integração real com o órgão, apenas a geração do protocolo.
     */
    @Transactional
    public RelatorioResponse enviarAoOrgao(Long id) {
        Relatorio relatorio = buscar(id);
        relatorio.registrarEnvio(String.format("ORG-%d-%06d", Year.now().getValue(), relatorio.getId()));
        return RelatorioResponse.de(relatorio);
    }

    private Relatorio buscar(Long id) {
        return relatorios.findById(id).orElseThrow(() -> new NaoEncontradoException("Relatório", id));
    }
}
