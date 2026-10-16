package br.com.carboncalc.dto;

import java.time.LocalDateTime;

import br.com.carboncalc.domain.Formato;
import br.com.carboncalc.domain.Relatorio;
import br.com.carboncalc.domain.TipoRelatorio;
import jakarta.validation.constraints.NotNull;

public final class RelatorioDtos {

    private RelatorioDtos() {
    }

    public record RelatorioRequest(@NotNull Long calculoId, @NotNull TipoRelatorio tipo,
            @NotNull Formato formato) {
    }

    public record RelatorioResponse(Long id, Long empresaId, Long calculoId, String cenario, TipoRelatorio tipo,
            String rotulo, Formato formato, LocalDateTime geradoEm, String protocoloEnvio,
            LocalDateTime enviadoEm) {

        public static RelatorioResponse de(Relatorio relatorio) {
            return new RelatorioResponse(relatorio.getId(), relatorio.getEmpresa().getId(),
                    relatorio.getCalculo().getId(), relatorio.getCalculo().getCenario(), relatorio.getTipo(),
                    relatorio.getTipo().getRotulo(), relatorio.getFormato(), relatorio.getGeradoEm(),
                    relatorio.getProtocoloEnvio(), relatorio.getEnviadoEm());
        }
    }

    public record Arquivo(String nome, String contentType, byte[] conteudo) {
    }
}
