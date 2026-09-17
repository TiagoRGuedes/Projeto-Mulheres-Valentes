package com.ibmec.mulheresvalentes.repository;

import com.ibmec.mulheresvalentes.model.AcaoHistorico;
import com.ibmec.mulheresvalentes.model.HistoricoOperacao;
import com.ibmec.mulheresvalentes.model.ModuloHistorico;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface HistoricoOperacaoRepository extends JpaRepository<HistoricoOperacao, Long> {

    List<HistoricoOperacao> findTop8ByOrderByDataHoraDescIdDesc();

    List<HistoricoOperacao> findTop8ByTipoEntidadeAndEntidadeIdOrderByDataHoraDescIdDesc(
            String tipoEntidade, Long entidadeId);

    List<HistoricoOperacao> findByTipoEntidadeAndEntidadeIdOrderByDataHoraDescIdDesc(
            String tipoEntidade, Long entidadeId);

    @Query("""
            select h
              from HistoricoOperacao h
             where (:inicio is null or h.dataHora >= :inicio)
               and (:fimExclusivo is null or h.dataHora < :fimExclusivo)
               and (:modulo is null or h.modulo = :modulo)
               and (:acao is null or h.acao = :acao)
               and (:busca is null
                    or lower(h.nomeEntidade) like concat('%', :busca, '%')
                    or lower(h.descricao) like concat('%', :busca, '%'))
             order by h.dataHora desc, h.id desc
            """)
    Page<HistoricoOperacao> pesquisar(
            @Param("inicio") LocalDateTime inicio,
            @Param("fimExclusivo") LocalDateTime fimExclusivo,
            @Param("modulo") ModuloHistorico modulo,
            @Param("acao") AcaoHistorico acao,
            @Param("busca") String busca,
            Pageable pageable);
}
