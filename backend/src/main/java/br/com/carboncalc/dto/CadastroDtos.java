package br.com.carboncalc.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import br.com.carboncalc.domain.AcaoPlanta;
import br.com.carboncalc.domain.Empresa;
import br.com.carboncalc.domain.Escopo;
import br.com.carboncalc.domain.FatorEmissao;
import br.com.carboncalc.domain.FonteEmissao;
import br.com.carboncalc.domain.Planta;
import br.com.carboncalc.domain.StatusPlanta;
import br.com.carboncalc.domain.TipoFonte;
import br.com.carboncalc.domain.TipoPlanta;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public final class CadastroDtos {

    private CadastroDtos() {
    }

    public record EmpresaRequest(
            @NotBlank String nome,
            @NotBlank @Pattern(regexp = "\\d{14}", message = "o CNPJ deve ter 14 dígitos") String cnpj,
            @NotBlank String setor) {
    }

    public record EmpresaResponse(Long id, String nome, String cnpj, String setor) {

        public static EmpresaResponse de(Empresa empresa) {
            return new EmpresaResponse(empresa.getId(), empresa.getNome(), empresa.getCnpj(), empresa.getSetor());
        }
    }

    public record PlantaRequest(@NotBlank String nome, @NotBlank String localizacao, @NotNull TipoPlanta tipo) {
    }

    /** {@code subestado} só existe para plantas ativas: OPERANDO ou OCIOSA. */
    public record PlantaResponse(Long id, Long empresaId, String nome, String localizacao, TipoPlanta tipo,
            StatusPlanta status, String subestado, List<AcaoPlanta> acoesDisponiveis) {

        public static PlantaResponse de(Planta planta, String subestado) {
            return new PlantaResponse(planta.getId(), planta.getEmpresa().getId(), planta.getNome(),
                    planta.getLocalizacao(), planta.getTipo(), planta.getStatus(), subestado,
                    planta.acoesDisponiveis());
        }
    }

    /** {@code documentacaoOk} só é considerado na ação ATIVAR. */
    public record TransicaoRequest(@NotNull AcaoPlanta acao, Boolean documentacaoOk) {
    }

    public record FonteRequest(@NotNull TipoFonte tipo, @NotBlank String descricao) {
    }

    public record FonteResponse(Long id, Long plantaId, TipoFonte tipo, String rotulo, Escopo escopo,
            String unidade, String descricao) {

        public static FonteResponse de(FonteEmissao fonte) {
            TipoFonte tipo = fonte.getTipo();
            return new FonteResponse(fonte.getId(), fonte.getPlanta().getId(), tipo, tipo.getRotulo(),
                    tipo.getEscopo(), tipo.getUnidade(), fonte.getDescricao());
        }
    }

    public record FatorResponse(TipoFonte tipo, String rotulo, Escopo escopo, String unidade, BigDecimal valor,
            LocalDate vigenciaInicio) {

        public static FatorResponse de(FatorEmissao fator) {
            TipoFonte tipo = fator.getTipoFonte();
            return new FatorResponse(tipo, tipo.getRotulo(), tipo.getEscopo(), fator.getUnidade(),
                    fator.getValor(), fator.getVigenciaInicio());
        }
    }
}
