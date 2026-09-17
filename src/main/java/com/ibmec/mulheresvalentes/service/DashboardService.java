package com.ibmec.mulheresvalentes.service;

import com.ibmec.mulheresvalentes.dto.DashboardEncontroRecenteDTO;
import com.ibmec.mulheresvalentes.dto.DashboardFinanceiroDTO;
import com.ibmec.mulheresvalentes.dto.DashboardPresencaDTO;
import com.ibmec.mulheresvalentes.dto.DashboardResumoDTO;
import com.ibmec.mulheresvalentes.dto.DashboardVisaoGeralDTO;
import com.ibmec.mulheresvalentes.model.Encontro;
import com.ibmec.mulheresvalentes.model.StatusPresenca;
import com.ibmec.mulheresvalentes.model.TipoLancamento;
import com.ibmec.mulheresvalentes.repository.AtividadeRepository;
import com.ibmec.mulheresvalentes.repository.EncontroRepository;
import com.ibmec.mulheresvalentes.repository.LancamentoCaixaRepository;
import com.ibmec.mulheresvalentes.repository.ParticipanteRepository;
import com.ibmec.mulheresvalentes.repository.PresencaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

@Service
public class DashboardService {

    private static final BigDecimal ZERO_MOEDA = new BigDecimal("0.00");

    private final ParticipanteRepository participanteRepository;
    private final AtividadeRepository atividadeRepository;
    private final EncontroRepository encontroRepository;
    private final PresencaRepository presencaRepository;
    private final LancamentoCaixaRepository lancamentoRepository;
    private final HistoricoOperacaoService historicoService;

    public DashboardService(
            ParticipanteRepository participanteRepository,
            AtividadeRepository atividadeRepository,
            EncontroRepository encontroRepository,
            PresencaRepository presencaRepository,
            LancamentoCaixaRepository lancamentoRepository,
            HistoricoOperacaoService historicoService) {
        this.participanteRepository = participanteRepository;
        this.atividadeRepository = atividadeRepository;
        this.encontroRepository = encontroRepository;
        this.presencaRepository = presencaRepository;
        this.lancamentoRepository = lancamentoRepository;
        this.historicoService = historicoService;
    }

    @Transactional(readOnly = true)
    public DashboardResumoDTO carregar() {
        LocalDate hoje = LocalDate.now();
        LocalDate inicioMes = hoje.withDayOfMonth(1);
        LocalDate fimMes = hoje.withDayOfMonth(hoje.lengthOfMonth());

        long registrosMes = presencaRepository.contarPorPeriodo(inicioMes, fimMes);
        long presentesMes = presencaRepository.contarPorPeriodoEStatus(
                inicioMes, fimMes, StatusPresenca.PRESENTE);
        long ausentesMes = presencaRepository.contarPorPeriodoEStatus(
                inicioMes, fimMes, StatusPresenca.AUSENTE);
        long justificadasMes = presencaRepository.contarPorPeriodoEStatus(
                inicioMes, fimMes, StatusPresenca.JUSTIFICADA);

        DashboardVisaoGeralDTO visaoGeral = new DashboardVisaoGeralDTO(
                participanteRepository.countByAtivoTrue(),
                atividadeRepository.countByAtivoTrue(),
                encontroRepository.countByDataBetween(inicioMes, fimMes),
                registrosMes);

        Encontro ultimoEncontro = encontroRepository
                .findFirstByDataLessThanEqualOrderByDataDescIdDesc(hoje)
                .orElse(null);
        long registrosUltimo = ultimoEncontro == null
                ? 0
                : presencaRepository.countByEncontroId(ultimoEncontro.getId());
        long presentesUltimo = ultimoEncontro == null
                ? 0
                : presencaRepository.countByEncontroIdAndStatus(
                        ultimoEncontro.getId(), StatusPresenca.PRESENTE);
        DashboardPresencaDTO presenca = new DashboardPresencaDTO(
                presentesMes,
                ausentesMes,
                justificadasMes,
                percentual(presentesMes, registrosMes),
                ultimoEncontro == null ? null : ultimoEncontro.getId(),
                ultimoEncontro == null ? null : ultimoEncontro.getAtividade().getNome(),
                ultimoEncontro == null ? null : ultimoEncontro.getData(),
                presentesUltimo,
                registrosUltimo,
                percentual(presentesUltimo, registrosUltimo));

        BigDecimal entradasMes = somar(TipoLancamento.ENTRADA, inicioMes, fimMes);
        BigDecimal saidasMes = somar(TipoLancamento.SAIDA, inicioMes, fimMes);
        BigDecimal saldoAtual = somar(TipoLancamento.ENTRADA, null, null)
                .subtract(somar(TipoLancamento.SAIDA, null, null))
                .setScale(2, RoundingMode.UNNECESSARY);
        DashboardFinanceiroDTO financeiro = new DashboardFinanceiroDTO(
                saldoAtual,
                entradasMes,
                saidasMes,
                lancamentoRepository.countByCanceladoFalseAndDataBetween(inicioMes, fimMes));

        List<DashboardEncontroRecenteDTO> encontrosRecentes = encontroRepository
                .findTop5ByOrderByDataDescIdDesc()
                .stream()
                .map(encontro -> {
                    long registros = presencaRepository.countByEncontroId(encontro.getId());
                    String situacao = registros == 0
                            ? "Presença ainda não registrada"
                            : registros + (registros == 1 ? " registro salvo" : " registros salvos");
                    return new DashboardEncontroRecenteDTO(
                            encontro.getId(),
                            encontro.getAtividade().getId(),
                            encontro.getAtividade().getNome(),
                            encontro.getData(),
                            registros,
                            situacao);
                })
                .toList();

        return new DashboardResumoDTO(
                visaoGeral,
                presenca,
                financeiro,
                lancamentoRepository.findTop5ByCanceladoFalseOrderByDataDescIdDesc(),
                encontrosRecentes,
                historicoService.listarRecentes());
    }

    private BigDecimal somar(TipoLancamento tipo, LocalDate inicio, LocalDate fim) {
        BigDecimal valor = lancamentoRepository.somarAtivosPorTipoEPeriodo(tipo, inicio, fim);
        return (valor == null ? ZERO_MOEDA : valor).setScale(2, RoundingMode.UNNECESSARY);
    }

    private BigDecimal percentual(long parte, long total) {
        if (total == 0) {
            return BigDecimal.ZERO.setScale(1);
        }
        return BigDecimal.valueOf(parte)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(total), 1, RoundingMode.HALF_UP);
    }
}
