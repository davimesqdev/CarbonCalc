package br.com.carboncalc.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.carboncalc.domain.Compensacao;

public interface CompensacaoRepository extends JpaRepository<Compensacao, Long> {

    List<Compensacao> findByCalculoIdOrderById(Long calculoId);
}
