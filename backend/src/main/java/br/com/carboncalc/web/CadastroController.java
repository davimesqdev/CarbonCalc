package br.com.carboncalc.web;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import br.com.carboncalc.dto.CadastroDtos.EmpresaRequest;
import br.com.carboncalc.dto.CadastroDtos.EmpresaResponse;
import br.com.carboncalc.dto.CadastroDtos.FatorResponse;
import br.com.carboncalc.dto.CadastroDtos.FonteRequest;
import br.com.carboncalc.dto.CadastroDtos.FonteResponse;
import br.com.carboncalc.dto.CadastroDtos.PlantaRequest;
import br.com.carboncalc.dto.CadastroDtos.PlantaResponse;
import br.com.carboncalc.dto.CadastroDtos.TransicaoRequest;
import br.com.carboncalc.service.CadastroService;
import br.com.carboncalc.service.FatorEmissaoService;
import jakarta.validation.Valid;

/** Cadastros (UC03 a UC05). A leitura é liberada a todos os perfis; a escrita, ao administrador. */
@RestController
@RequestMapping("/api")
public class CadastroController {

    private final CadastroService cadastro;
    private final FatorEmissaoService fatores;

    public CadastroController(CadastroService cadastro, FatorEmissaoService fatores) {
        this.cadastro = cadastro;
        this.fatores = fatores;
    }

    @GetMapping("/empresas")
    List<EmpresaResponse> listarEmpresas() {
        return cadastro.listarEmpresas();
    }

    @PostMapping("/empresas")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    EmpresaResponse cadastrarEmpresa(@Valid @RequestBody EmpresaRequest request) {
        return cadastro.cadastrarEmpresa(request);
    }

    @GetMapping("/empresas/{empresaId}/plantas")
    List<PlantaResponse> listarPlantas(@PathVariable Long empresaId) {
        return cadastro.listarPlantas(empresaId);
    }

    @PostMapping("/empresas/{empresaId}/plantas")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    PlantaResponse cadastrarPlanta(@PathVariable Long empresaId, @Valid @RequestBody PlantaRequest request) {
        return cadastro.cadastrarPlanta(empresaId, request);
    }

    @PostMapping("/plantas/{plantaId}/transicao")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    PlantaResponse transicionarPlanta(@PathVariable Long plantaId, @Valid @RequestBody TransicaoRequest request) {
        return cadastro.transicionarPlanta(plantaId, request);
    }

    @GetMapping("/plantas/{plantaId}/fontes")
    List<FonteResponse> listarFontes(@PathVariable Long plantaId) {
        return cadastro.listarFontes(plantaId);
    }

    @PostMapping("/plantas/{plantaId}/fontes")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    FonteResponse registrarFonte(@PathVariable Long plantaId, @Valid @RequestBody FonteRequest request) {
        return cadastro.registrarFonte(plantaId, request);
    }

    @GetMapping("/fatores")
    List<FatorResponse> listarFatores() {
        return fatores.listarVigentes();
    }
}
