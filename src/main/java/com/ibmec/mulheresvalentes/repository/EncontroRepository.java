package com.ibmec.mulheresvalentes.repository;

import com.ibmec.mulheresvalentes.model.Encontro;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface EncontroRepository extends JpaRepository<Encontro, Long> {

    @Override
    @EntityGraph(attributePaths = "atividade")
    Optional<Encontro> findById(Long id);

    @EntityGraph(attributePaths = "atividade")
    List<Encontro> findAllByOrderByDataDescIdDesc();

    @EntityGraph(attributePaths = "atividade")
    List<Encontro> findByAtividadeIdOrderByDataDescIdDesc(Long atividadeId);

    @EntityGraph(attributePaths = "atividade")
    List<Encontro> findByDataBetweenOrderByDataDescIdDesc(LocalDate dataInicial, LocalDate dataFinal);

    long countByDataBetween(LocalDate dataInicial, LocalDate dataFinal);

    long countByAtividadeId(Long atividadeId);

    boolean existsByAtividadeId(Long atividadeId);

    @EntityGraph(attributePaths = "atividade")
    Optional<Encontro> findFirstByDataLessThanEqualOrderByDataDescIdDesc(LocalDate data);

    @EntityGraph(attributePaths = "atividade")
    List<Encontro> findTop5ByOrderByDataDescIdDesc();

    @EntityGraph(attributePaths = "atividade")
    @Query("""
            select e
             from Encontro e
             where lower(e.atividade.nome) like concat('%', lower(:termo), '%')
             order by e.data desc, e.id desc
            """)
    List<Encontro> buscarGlobal(@Param("termo") String termo, Pageable limite);
}
