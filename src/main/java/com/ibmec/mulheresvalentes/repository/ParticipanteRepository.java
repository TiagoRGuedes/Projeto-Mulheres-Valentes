package com.ibmec.mulheresvalentes.repository;

import com.ibmec.mulheresvalentes.model.Participante;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.time.LocalDateTime;

public interface ParticipanteRepository extends JpaRepository<Participante, Long> {

    List<Participante> findAllByOrderByNomeAsc();

    List<Participante> findByAtivoTrueOrderByNomeAsc();

    List<Participante> findByNomeContainingIgnoreCaseOrderByNomeAsc(String nome);

    @Query("""
            select p
             from Participante p
             where lower(p.nome) like concat('%', lower(:termo), '%')
             order by p.nome asc, p.id asc
            """)
    List<Participante> buscarGlobal(@Param("termo") String termo, Pageable limite);

    long countByAtivoTrue();

    long countByAtivoFalse();

    long countByDataCadastroGreaterThanEqual(LocalDateTime inicio);
}
