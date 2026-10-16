package br.com.carboncalc.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class PlantaTest {

    private static Planta novaPlanta() {
        return new Planta(new Empresa("Acme", "12345678000199", "Indústria"), "Unidade 1", "Campinas/SP",
                TipoPlanta.EXISTENTE);
    }

    private static Planta plantaAtiva() {
        Planta planta = novaPlanta();
        planta.confirmarDocumentacao();
        planta.aplicar(AcaoPlanta.ATIVAR);
        return planta;
    }

    @Test
    void comecaCadastrada() {
        assertThat(novaPlanta().getStatus()).isEqualTo(StatusPlanta.CADASTRADA);
    }

    @Test
    void naoAtivaSemDocumentacao() {
        Planta planta = novaPlanta();

        assertThatThrownBy(() -> planta.aplicar(AcaoPlanta.ATIVAR)).isInstanceOf(RegraNegocioException.class);
        assertThat(planta.getStatus()).isEqualTo(StatusPlanta.CADASTRADA);
    }

    @Test
    void percorreManutencaoEInativacao() {
        Planta planta = plantaAtiva();

        planta.aplicar(AcaoPlanta.ENTRAR_MANUTENCAO);
        assertThat(planta.getStatus()).isEqualTo(StatusPlanta.EM_MANUTENCAO);
        planta.aplicar(AcaoPlanta.SAIR_MANUTENCAO);
        assertThat(planta.getStatus()).isEqualTo(StatusPlanta.ATIVA);

        planta.aplicar(AcaoPlanta.INATIVAR);
        assertThat(planta.getStatus()).isEqualTo(StatusPlanta.INATIVA);
        assertThat(planta.getInativadaEm()).isNotNull();
        planta.aplicar(AcaoPlanta.REATIVAR);
        assertThat(planta.getStatus()).isEqualTo(StatusPlanta.ATIVA);
        assertThat(planta.getInativadaEm()).isNull();
    }

    @Test
    void recusaTransicaoForaDoDiagrama() {
        Planta planta = plantaAtiva();
        planta.aplicar(AcaoPlanta.ENTRAR_MANUTENCAO);

        assertThatThrownBy(() -> planta.aplicar(AcaoPlanta.INATIVAR)).isInstanceOf(RegraNegocioException.class);
    }

    @Test
    void cancelarCadastroArquivaEEncerraOCiclo() {
        Planta planta = novaPlanta();

        planta.aplicar(AcaoPlanta.CANCELAR_CADASTRO);

        assertThat(planta.getStatus()).isEqualTo(StatusPlanta.ARQUIVADA);
        assertThat(planta.acoesDisponiveis()).isEmpty();
    }

    @Test
    void arquivamentoAutomaticoSoParaInativas() {
        Planta ativa = plantaAtiva();
        assertThatThrownBy(ativa::arquivarAutomaticamente).isInstanceOf(RegraNegocioException.class);

        ativa.aplicar(AcaoPlanta.INATIVAR);
        ativa.arquivarAutomaticamente();
        assertThat(ativa.getStatus()).isEqualTo(StatusPlanta.ARQUIVADA);
    }
}
