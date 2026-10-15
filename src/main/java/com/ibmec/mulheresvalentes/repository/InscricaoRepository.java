package com.ibmec.mulheresvalentes.repository;

import com.ibmec.mulheresvalentes.model.Inscricao;
import com.ibmec.mulheresvalentes.model.StatusInscricao;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface InscricaoRepository extends JpaRepository<Inscricao, Long> {

    @EntityGraph(attributePaths = {"participante", "areas"})
    List<Inscricao> findAllByOrderByRecebidaEmDescIdDesc();

    @EntityGraph(attributePaths = {"participante", "areas"})
    List<Inscricao> findByStatusOrderByRecebidaEmDescIdDesc(StatusInscricao status);

    @Override
    @EntityGraph(attributePaths = {"participante", "areas"})
    Optional<Inscricao> findById(Long id);

    long countByStatus(StatusInscricao status);

    long countByStatusIn(Collection<StatusInscricao> status);

    boolean existsByParticipanteId(Long participanteId);
}
