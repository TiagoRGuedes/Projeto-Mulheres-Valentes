package com.ibmec.mulheresvalentes.service;

import com.ibmec.mulheresvalentes.dto.AtividadeConectadaDTO;
import com.ibmec.mulheresvalentes.dto.EncontroConectadoDTO;
import com.ibmec.mulheresvalentes.dto.FrequenciaParticipanteDto;
import com.ibmec.mulheresvalentes.dto.ParticipanteConectadoDTO;
import com.ibmec.mulheresvalentes.model.Encontro;
import com.ibmec.mulheresvalentes.model.Participante;
import com.ibmec.mulheresvalentes.model.Presenca;
import com.ibmec.mulheresvalentes.model.StatusPresenca;
import com.ibmec.mulheresvalentes.repository.EncontroRepository;
import com.ibmec.mulheresvalentes.repository.ParticipanteRepository;
import com.ibmec.mulheresvalentes.repository.PresencaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class ConexoesAdministrativasService {

    private final ParticipanteRepository participanteRepository;
    private final EncontroRepository encontroRepository;
    private final PresencaRepository presencaRepository;

    public ConexoesAdministrativasService(
            ParticipanteRepository participanteRepository,
            EncontroRepository encontroRepository,
            PresencaRepository presencaRepository) {
        this.participanteRepository = participanteRepository;
        this.encontroRepository = encontroRepository;
        this.presencaRepository = presencaRepository;
    }

    @Transactional(readOnly = true)
    public ParticipanteConectadoDTO participante(Long participanteId) {
        Participante participante = participanteRepository.findById(participanteId)
                .orElseThrow();
        List<Presenca> registros = presencaRepository
                .findByParticipanteIdOrderByDataDescIdDesc(participanteId);
        long presentes = contar(registros, StatusPresenca.PRESENTE);
        long ausentes = contar(registros, StatusPresenca.AUSENTE);
        long justificadas = contar(registros, StatusPresenca.JUSTIFICADA);
        BigDecimal percentual = registros.isEmpty()
                ? BigDecimal.ZERO.setScale(1)
                : BigDecimal.valueOf(presentes)
                        .multiply(BigDecimal.valueOf(100))
                        .divide(BigDecimal.valueOf(registros.size()), 1, RoundingMode.HALF_UP);
        FrequenciaParticipanteDto frequencia = new FrequenciaParticipanteDto(
                participante.getId(), participante.getNome(), registros.size(),
                presentes, ausentes, justificadas, percentual);
        return new ParticipanteConectadoDTO(frequencia, registros.stream().limit(5).toList());
    }

    @Transactional(readOnly = true)
    public AtividadeConectadaDTO atividade(Long atividadeId) {
        List<Encontro> encontros = encontroRepository
                .findByAtividadeIdOrderByDataDescIdDesc(atividadeId);
        List<Presenca> registros = presencaRepository.findAllComRelacionamentos().stream()
                .filter(presenca -> presenca.getEncontro() != null
                        && presenca.getEncontro().getAtividade().getId().equals(atividadeId))
                .toList();
        return new AtividadeConectadaDTO(
                encontros.size(),
                resumir(registros),
                encontros.stream().limit(5).toList());
    }

    @Transactional(readOnly = true)
    public EncontroConectadoDTO encontro(Long encontroId) {
        List<Presenca> registros = presencaRepository
                .findByEncontroIdOrderByParticipanteNomeAsc(encontroId);
        return new EncontroConectadoDTO(resumir(registros), registros);
    }

    private com.ibmec.mulheresvalentes.dto.ResumoPresencasDto resumir(List<Presenca> registros) {
        long presentes = contar(registros, StatusPresenca.PRESENTE);
        long ausentes = contar(registros, StatusPresenca.AUSENTE);
        long justificadas = contar(registros, StatusPresenca.JUSTIFICADA);
        return new com.ibmec.mulheresvalentes.dto.ResumoPresencasDto(
                registros.size(), presentes, ausentes, justificadas);
    }

    private long contar(List<Presenca> registros, StatusPresenca status) {
        return registros.stream().filter(registro -> registro.getStatus() == status).count();
    }
}
