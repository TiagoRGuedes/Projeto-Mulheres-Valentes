package com.ibmec.mulheresvalentes.repository;

import com.ibmec.mulheresvalentes.model.Presenca;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface PresencaRepository extends JpaRepository<Presenca, Long> {

    @Override
    @EntityGraph(attributePaths = {"participante", "encontro", "encontro.atividade"})
    Optional<Presenca> findById(Long id);

    boolean existsByParticipanteIdAndEncontroId(Long participanteId, Long encontroId);

    boolean existsByParticipanteId(Long participanteId);

    boolean existsByEncontroId(Long encontroId);

    Optional<Presenca> findByParticipanteIdAndEncontroId(Long participanteId, Long encontroId);

    @EntityGraph(attributePaths = {"participante", "encontro", "encontro.atividade"})
    List<Presenca> findByEncontroIdOrderByParticipanteNomeAsc(Long encontroId);

    @EntityGraph(attributePaths = {"participante", "encontro", "encontro.atividade"})
    List<Presenca> findByParticipanteIdOrderByEncontroDataDesc(Long participanteId);

    @Query("""
            select p
              from Presenca p
              join fetch p.participante
              join fetch p.encontro e
              join fetch e.atividade
             order by e.data desc, p.id desc
            """)
    List<Presenca> findAllComRelacionamentos();

    @Query("select count(p) from Presenca p where p.encontro.data between :inicio and :fim")
    long contarPorPeriodo(@Param("inicio") LocalDate inicio, @Param("fim") LocalDate fim);

    @Query("""
            select count(p) from Presenca p
             where p.encontro.data between :inicio and :fim
               and p.status = :status
            """)
    long contarPorPeriodoEStatus(
            @Param("inicio") LocalDate inicio,
            @Param("fim") LocalDate fim,
            @Param("status") com.ibmec.mulheresvalentes.model.StatusPresenca status);

    long countByEncontroId(Long encontroId);

    long countByEncontroIdAndStatus(Long encontroId, com.ibmec.mulheresvalentes.model.StatusPresenca status);

    @Query("select count(distinct p.encontro.id) from Presenca p")
    long contarEncontrosComPresenca();
}
