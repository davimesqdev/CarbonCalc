package br.com.carboncalc.web;

import java.util.List;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import br.com.carboncalc.dto.RelatorioDtos.Arquivo;
import br.com.carboncalc.dto.RelatorioDtos.RelatorioRequest;
import br.com.carboncalc.dto.RelatorioDtos.RelatorioResponse;
import br.com.carboncalc.service.RelatorioService;
import jakarta.validation.Valid;

/** Relatórios (UC11 a UC13). Gerar é permitido a todos os perfis; enviar ao órgão, à gestão. */
@RestController
@RequestMapping("/api/relatorios")
public class RelatorioController {

    private final RelatorioService relatorios;

    public RelatorioController(RelatorioService relatorios) {
        this.relatorios = relatorios;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    RelatorioResponse gerar(@Valid @RequestBody RelatorioRequest request) {
        return relatorios.gerar(request);
    }

    @GetMapping
    List<RelatorioResponse> listar(@RequestParam Long empresaId) {
        return relatorios.listar(empresaId);
    }

    @GetMapping("/{id}/download")
    ResponseEntity<byte[]> baixar(@PathVariable Long id) {
        Arquivo arquivo = relatorios.baixar(id);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(arquivo.contentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(arquivo.nome()).build().toString())
                .body(arquivo.conteudo());
    }

    @PostMapping("/{id}/enviar")
    @PreAuthorize("hasAnyRole('GESTOR', 'ADMINISTRADOR')")
    RelatorioResponse enviarAoOrgao(@PathVariable Long id) {
        return relatorios.enviarAoOrgao(id);
    }
}
