package br.com.carboncalc.web;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import br.com.carboncalc.dto.CalculoDtos.CalculoRequest;
import br.com.carboncalc.dto.CalculoDtos.CalculoResponse;
import br.com.carboncalc.dto.CalculoDtos.ComparacaoResponse;
import br.com.carboncalc.dto.CalculoDtos.CompensacaoRequest;
import br.com.carboncalc.dto.CalculoDtos.ConsumoRequest;
import br.com.carboncalc.dto.CalculoDtos.ConsumoResponse;
import br.com.carboncalc.dto.CalculoDtos.PlanoCompensacaoResponse;
import br.com.carboncalc.service.CalculoService;
import br.com.carboncalc.service.CompensacaoService;
import jakarta.validation.Valid;

/** Consumo, cálculo, comparação e compensação (UC06 a UC10) e consulta ao histórico (UC14). */
@RestController
@RequestMapping("/api")
public class CalculoController {

    private static final String GESTAO = "hasAnyRole('GESTOR', 'ADMINISTRADOR')";

    private final CalculoService calculos;
    private final CompensacaoService compensacoes;

    public CalculoController(CalculoService calculos, CompensacaoService compensacoes) {
        this.calculos = calculos;
        this.compensacoes = compensacoes;
    }

    @GetMapping("/empresas/{empresaId}/consumos")
    List<ConsumoResponse> listarConsumos(@PathVariable Long empresaId) {
        return calculos.listarConsumos(empresaId);
    }

    @PostMapping("/consumos")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize(GESTAO)
    ConsumoResponse registrarConsumo(@Valid @RequestBody ConsumoRequest request) {
        return calculos.registrarConsumo(request);
    }

    @PostMapping("/calculos")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize(GESTAO)
    CalculoResponse calcular(@Valid @RequestBody CalculoRequest request) {
        return calculos.calcular(request);
    }

    @GetMapping("/calculos")
    List<CalculoResponse> historico(@RequestParam Long empresaId) {
        return calculos.historico(empresaId);
    }

    @GetMapping("/calculos/comparar")
    ComparacaoResponse comparar(@RequestParam Long base, @RequestParam Long alternativo) {
        return calculos.comparar(base, alternativo);
    }

    @GetMapping("/calculos/{id}")
    CalculoResponse buscar(@PathVariable Long id) {
        return calculos.buscar(id);
    }

    @GetMapping("/calculos/{id}/compensacoes")
    PlanoCompensacaoResponse buscarPlano(@PathVariable Long id) {
        return compensacoes.buscarPlano(id);
    }

    @PostMapping("/calculos/{id}/compensacoes")
    @PreAuthorize(GESTAO)
    PlanoCompensacaoResponse gerarPlano(@PathVariable Long id, @Valid @RequestBody CompensacaoRequest request) {
        return compensacoes.gerarPlano(id, request.percentual());
    }
}
