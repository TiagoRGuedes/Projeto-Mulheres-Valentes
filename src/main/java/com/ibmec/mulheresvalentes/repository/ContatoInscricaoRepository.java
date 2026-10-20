package com.ibmec.mulheresvalentes.repository;

import com.ibmec.mulheresvalentes.model.ContatoInscricao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ContatoInscricaoRepository extends JpaRepository<ContatoInscricao, Long> {

    List<ContatoInscricao> findByInscricaoIdOrderByRealizadoEmDescIdDesc(Long inscricaoId);

    long countByInscricaoId(Long inscricaoId);

    /** Para cada inscrição: id, data do último contato e quantidade de contatos. */
    @Query("""
            select c.inscricao.id, max(c.realizadoEm), count(c)
              from ContatoInscricao c
             group by c.inscricao.id
            """)
    List<Object[]> resumirPorInscricao();
}
