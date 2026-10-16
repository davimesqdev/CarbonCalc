package br.com.carboncalc.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.carboncalc.domain.Calculo;

public interface CalculoRepository extends JpaRepository<Calculo, Long> {

    List<Calculo> findByEmpresaIdOrderByDataCalculoDesc(Long empresaId);
}
