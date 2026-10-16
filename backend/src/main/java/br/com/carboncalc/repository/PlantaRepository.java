package br.com.carboncalc.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.carboncalc.domain.Planta;
import br.com.carboncalc.domain.StatusPlanta;

public interface PlantaRepository extends JpaRepository<Planta, Long> {

    List<Planta> findByEmpresaIdOrderByNome(Long empresaId);

    List<Planta> findByStatusAndInativadaEmBefore(StatusPlanta status, LocalDateTime limite);
}
