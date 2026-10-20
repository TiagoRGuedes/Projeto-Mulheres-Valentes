package com.ibmec.mulheresvalentes.repository;

import com.ibmec.mulheresvalentes.model.Notificacao;
import com.ibmec.mulheresvalentes.model.TipoNotificacao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface NotificacaoRepository extends JpaRepository<Notificacao, Long> {

    List<Notificacao> findTop50ByOrderByCriadaEmDescIdDesc();

    long countByCriadaEmAfter(LocalDateTime momento);

    boolean existsByTipoAndReferenciaId(TipoNotificacao tipo, Long referenciaId);
}
