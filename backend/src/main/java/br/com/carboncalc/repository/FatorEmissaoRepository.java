package br.com.carboncalc.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.carboncalc.domain.FatorEmissao;
import br.com.carboncalc.domain.TipoFonte;

public interface FatorEmissaoRepository extends JpaRepository<FatorEmissao, Long> {

    List<FatorEmissao> findByVigenciaFimIsNull();

    Optional<FatorEmissao> findByTipoFonteAndVigenciaFimIsNull(TipoFonte tipoFonte);
}
