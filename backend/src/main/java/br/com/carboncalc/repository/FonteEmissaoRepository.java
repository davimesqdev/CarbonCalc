package br.com.carboncalc.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.carboncalc.domain.FonteEmissao;

public interface FonteEmissaoRepository extends JpaRepository<FonteEmissao, Long> {

    List<FonteEmissao> findByPlantaIdOrderByDescricao(Long plantaId);
}
