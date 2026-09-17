package com.ibmec.mulheresvalentes.repository;

import com.ibmec.mulheresvalentes.model.LancamentoCaixa;
import com.ibmec.mulheresvalentes.model.TipoLancamento;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface LancamentoCaixaRepository extends JpaRepository<LancamentoCaixa, Long> {

    @EntityGraph(attributePaths = "categoria")
    List<LancamentoCaixa> findAllByCanceladoFalseOrderByDataDescIdDesc();

    @EntityGraph(attributePaths = "categoria")
    List<LancamentoCaixa> findTop5ByCanceladoFalseOrderByDataDescIdDesc();

    @EntityGraph(attributePaths = "categoria")
    Optional<LancamentoCaixa> findByIdAndCanceladoFalse(Long id);

    boolean existsByCategoriaId(Long categoriaId);

    long countByCategoriaIdAndCanceladoFalse(Long categoriaId);

    @EntityGraph(attributePaths = "categoria")
    @Query("""
            select l
              from LancamentoCaixa l
             where (:dataInicial is null or l.data >= :dataInicial)
               and (:dataFinal is null or l.data <= :dataFinal)
               and l.cancelado = false
               and (:tipo is null or l.tipo = :tipo)
               and (:categoriaId is null or l.categoria.id = :categoriaId)
               and (:descricao is null or lower(l.descricao) like concat('%', :descricao, '%'))
             order by l.data desc, l.id desc
            """)
    List<LancamentoCaixa> pesquisar(
            @Param("dataInicial") LocalDate dataInicial,
            @Param("dataFinal") LocalDate dataFinal,
            @Param("tipo") TipoLancamento tipo,
            @Param("categoriaId") Long categoriaId,
            @Param("descricao") String descricao);

    @Query("""
            select coalesce(sum(l.valor), 0)
              from LancamentoCaixa l
             where l.cancelado = false
               and l.tipo = :tipo
               and (:dataInicial is null or l.data >= :dataInicial)
               and (:dataFinal is null or l.data <= :dataFinal)
            """)
    java.math.BigDecimal somarAtivosPorTipoEPeriodo(
            @Param("tipo") TipoLancamento tipo,
            @Param("dataInicial") LocalDate dataInicial,
            @Param("dataFinal") LocalDate dataFinal);

    long countByCanceladoFalseAndDataBetween(LocalDate dataInicial, LocalDate dataFinal);

    @EntityGraph(attributePaths = "categoria")
    @Query("""
            select l
              from LancamentoCaixa l
             where l.cancelado = false
               and (lower(l.descricao) like concat('%', lower(:termo), '%')
                 or lower(l.categoria.nome) like concat('%', lower(:termo), '%'))
             order by l.data desc, l.id desc
            """)
    List<LancamentoCaixa> buscarGlobal(@Param("termo") String termo, Pageable limite);
}
