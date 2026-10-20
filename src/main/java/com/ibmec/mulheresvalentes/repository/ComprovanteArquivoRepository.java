package com.ibmec.mulheresvalentes.repository;

import com.ibmec.mulheresvalentes.model.ComprovanteArquivo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ComprovanteArquivoRepository extends JpaRepository<ComprovanteArquivo, Long> {

    Optional<ComprovanteArquivo> findByLancamentoId(Long lancamentoId);
}
