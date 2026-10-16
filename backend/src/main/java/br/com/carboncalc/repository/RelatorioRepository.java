package br.com.carboncalc.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.carboncalc.domain.Relatorio;

public interface RelatorioRepository extends JpaRepository<Relatorio, Long> {

    List<Relatorio> findByEmpresaIdOrderByGeradoEmDesc(Long empresaId);
}
