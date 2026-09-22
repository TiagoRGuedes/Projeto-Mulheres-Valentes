package com.ibmec.mulheresvalentes.service;

import com.ibmec.mulheresvalentes.model.HistoricoOperacao;
import com.ibmec.mulheresvalentes.repository.AtividadeRepository;
import com.ibmec.mulheresvalentes.repository.EncontroRepository;
import com.ibmec.mulheresvalentes.repository.LancamentoCaixaRepository;
import com.ibmec.mulheresvalentes.repository.ParticipanteRepository;
import com.ibmec.mulheresvalentes.repository.PresencaRepository;
import org.springframework.stereotype.Component;

@Component("historicoLink")
public class HistoricoLinkService {

    private final ParticipanteRepository participanteRepository;
    private final AtividadeRepository atividadeRepository;
    private final EncontroRepository encontroRepository;
    private final PresencaRepository presencaRepository;
    private final LancamentoCaixaRepository lancamentoRepository;

    public HistoricoLinkService(
            ParticipanteRepository participanteRepository,
            AtividadeRepository atividadeRepository,
            EncontroRepository encontroRepository,
            PresencaRepository presencaRepository,
            LancamentoCaixaRepository lancamentoRepository) {
        this.participanteRepository = participanteRepository;
        this.atividadeRepository = atividadeRepository;
        this.encontroRepository = encontroRepository;
        this.presencaRepository = presencaRepository;
        this.lancamentoRepository = lancamentoRepository;
    }

    public String url(HistoricoOperacao operacao) {
        if (operacao == null || operacao.getEntidadeId() == null) {
            return null;
        }
        Long id = operacao.getEntidadeId();
        return switch (operacao.getTipoEntidade()) {
            case "Participante" -> participanteRepository.existsById(id) ? "/participantes/" + id : null;
            case "Atividade" -> atividadeRepository.existsById(id) ? "/atividades/" + id : null;
            case "Encontro" -> encontroRepository.existsById(id) ? "/encontros/" + id : null;
            case "Presenca" -> presencaRepository.existsById(id) ? "/presencas/" + id + "/corrigir" : null;
            case "LancamentoCaixa" -> lancamentoRepository.existsById(id)
                    ? "/caixa/movimentacoes/" + id : null;
            default -> null;
        };
    }

    public String rotulo(HistoricoOperacao operacao) {
        if (operacao == null) {
            return "Ver registro";
        }
        return switch (operacao.getTipoEntidade()) {
            case "Participante" -> "Ver participante";
            case "Atividade" -> "Ver atividade";
            case "Encontro" -> "Ver encontro";
            case "Presenca" -> "Ver presença";
            case "LancamentoCaixa" -> "Ver lançamento";
            default -> "Ver registro";
        };
    }
}
