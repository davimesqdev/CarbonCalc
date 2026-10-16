package br.com.carboncalc.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.carboncalc.domain.Empresa;

public interface EmpresaRepository extends JpaRepository<Empresa, Long> {

    boolean existsByCnpj(String cnpj);
}
