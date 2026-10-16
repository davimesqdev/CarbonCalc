package br.com.carboncalc.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.carboncalc.domain.FatorEmissao;
import br.com.carboncalc.domain.OrigemFatores;
import br.com.carboncalc.domain.TipoFonte;
import br.com.carboncalc.dto.CadastroDtos.FatorResponse;
import br.com.carboncalc.integracao.FonteFatoresExterna;
import br.com.carboncalc.repository.FatorEmissaoRepository;

@Service
public class FatorEmissaoService {

    private static final String CACHE = "fatores";
    private static final String CHAVE = "vigentes";

    private static final Logger log = LoggerFactory.getLogger(FatorEmissaoService.class);

    private final FonteFatoresExterna fonteExterna;
    private final FatorEmissaoRepository repositorio;
    private final CacheManager cacheManager;
    private final long timeoutSegundos;

    public FatorEmissaoService(FonteFatoresExterna fonteExterna, FatorEmissaoRepository repositorio,
            CacheManager cacheManager, @Value("${carboncalc.ghg.timeout-segundos}") long timeoutSegundos) {
        this.fonteExterna = fonteExterna;
        this.repositorio = repositorio;
        this.cacheManager = cacheManager;
        this.timeoutSegundos = timeoutSegundos;
    }

    public record Fatores(Map<TipoFonte, BigDecimal> valores, OrigemFatores origem) {
    }

    /**
     * Fatores vigentes em kg CO2e por unidade. Consulta a fonte externa com timeout e, se ela
     * falhar, usa a última cópia local (cache e, na falta dele, o banco).
     */
    @Transactional
    public Fatores obterFatores() {
        try {
            Map<TipoFonte, BigDecimal> externos = CompletableFuture.supplyAsync(fonteExterna::buscarFatores)
                    .get(timeoutSegundos, TimeUnit.SECONDS);
            sincronizar(externos);
            gravarCache(externos);
            return new Fatores(externos, OrigemFatores.EXTERNO);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (ExecutionException | TimeoutException e) {
            log.warn("Fonte externa de fatores indisponível; usando a cópia local", e);
        }
        return new Fatores(carregarLocal(), OrigemFatores.CACHE_LOCAL);
    }

    @Transactional(readOnly = true)
    public List<FatorResponse> listarVigentes() {
        return repositorio.findByVigenciaFimIsNull().stream()
                .sorted(Comparator.comparing(FatorEmissao::getTipoFonte))
                .map(FatorResponse::de)
                .toList();
    }

    private void sincronizar(Map<TipoFonte, BigDecimal> externos) {
        LocalDate hoje = LocalDate.now();
        externos.forEach((tipo, valor) -> {
            var vigente = repositorio.findByTipoFonteAndVigenciaFimIsNull(tipo);
            if (vigente.isEmpty()) {
                repositorio.save(new FatorEmissao(tipo, valor, hoje));
            } else if (vigente.get().getValor().compareTo(valor) != 0) {
                repositorio.save(vigente.get().atualizar(valor, hoje));
            }
        });
    }

    @SuppressWarnings("unchecked")
    private Map<TipoFonte, BigDecimal> carregarLocal() {
        try {
            Cache cache = cacheManager.getCache(CACHE);
            Map<TipoFonte, BigDecimal> emCache = cache == null ? null : cache.get(CHAVE, Map.class);
            if (emCache != null) {
                return emCache;
            }
        } catch (RuntimeException e) {
            log.warn("Cache de fatores indisponível; lendo do banco", e);
        }
        Map<TipoFonte, BigDecimal> doBanco = new EnumMap<>(TipoFonte.class);
        repositorio.findByVigenciaFimIsNull().forEach(f -> doBanco.put(f.getTipoFonte(), f.getValor()));
        return doBanco;
    }

    private void gravarCache(Map<TipoFonte, BigDecimal> fatores) {
        try {
            Cache cache = cacheManager.getCache(CACHE);
            if (cache != null) {
                cache.put(CHAVE, new EnumMap<>(fatores));
            }
        } catch (RuntimeException e) {
            log.warn("Não foi possível gravar os fatores no cache", e);
        }
    }
}
