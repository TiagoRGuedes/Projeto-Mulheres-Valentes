package com.ibmec.mulheresvalentes.repository;

import com.ibmec.mulheresvalentes.model.Atividade;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AtividadeRepository extends JpaRepository<Atividade, Long> {

    List<Atividade> findAllByOrderByNomeAsc();

    List<Atividade> findByAtivoTrueOrderByNomeAsc();

    List<Atividade> findByNomeContainingIgnoreCaseOrderByNomeAsc(String nome);

    @Query("""
            select a
              from Atividade a
             where lower(a.nome) like concat('%', lower(:termo), '%')
                or lower(coalesce(a.descricao, '')) like concat('%', lower(:termo), '%')
             order by a.nome asc, a.id asc
            """)
    List<Atividade> buscarGlobal(@Param("termo") String termo, Pageable limite);

    long countByAtivoTrue();

    long countByAtivoFalse();
}
