package br.com.carboncalc.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import br.com.carboncalc.domain.Consumo;

public interface ConsumoRepository extends JpaRepository<Consumo, Long> {

    @Query("""
            select c from Consumo c join fetch c.fonte f join fetch f.planta p
            where p.empresa.id = :empresaId and c.competencia between :inicio and :fim
            """)
    List<Consumo> buscarDoPeriodo(Long empresaId, LocalDate inicio, LocalDate fim);

    @Query("""
            select c from Consumo c join fetch c.fonte f join fetch f.planta p
            where p.empresa.id = :empresaId
            order by c.competencia desc, c.id desc
            """)
    List<Consumo> listarDaEmpresa(Long empresaId);

    boolean existsByFontePlantaIdAndCompetenciaGreaterThanEqual(Long plantaId, LocalDate desde);
}
