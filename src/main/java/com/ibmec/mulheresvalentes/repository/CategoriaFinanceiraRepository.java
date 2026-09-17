package com.ibmec.mulheresvalentes.repository;

import com.ibmec.mulheresvalentes.model.CategoriaFinanceira;
import com.ibmec.mulheresvalentes.model.TipoLancamento;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CategoriaFinanceiraRepository extends JpaRepository<CategoriaFinanceira, Long> {

    List<CategoriaFinanceira> findAllByOrderByTipoAscNomeAsc();

    List<CategoriaFinanceira> findByAtivoTrueOrderByTipoAscNomeAsc();

    List<CategoriaFinanceira> findByTipoAndAtivoTrueOrderByNomeAsc(TipoLancamento tipo);

    Optional<CategoriaFinanceira> findByNomeIgnoreCaseAndTipo(String nome, TipoLancamento tipo);

    boolean existsByNomeIgnoreCaseAndTipoAndIdNot(String nome, TipoLancamento tipo, Long id);

    @Query("""
            select c
              from CategoriaFinanceira c
             where lower(c.nome) like concat('%', lower(:termo), '%')
             order by c.ativo desc, c.tipo asc, c.nome asc, c.id asc
            """)
    List<CategoriaFinanceira> buscarGlobal(@Param("termo") String termo, Pageable limite);
}
