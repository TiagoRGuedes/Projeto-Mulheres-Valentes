package com.ibmec.mulheresvalentes.config;

import com.ibmec.mulheresvalentes.service.NotificacaoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * Tarefa automática: de hora em hora (e um minuto depois de iniciar), procura
 * inscrições que estão há mais de app.inscricoes.dias-sem-contato dias sem contato
 * e cria um aviso no sino. Desligue com app.notificacoes.verificacao-automatica=false.
 */
@Configuration
@EnableScheduling
@ConditionalOnProperty(name = "app.notificacoes.verificacao-automatica", havingValue = "true", matchIfMissing = true)
public class AgendamentoConfig {

    private static final Logger LOG = LoggerFactory.getLogger(AgendamentoConfig.class);

    private final NotificacaoService notificacaoService;
    private final int diasSemContato;

    public AgendamentoConfig(
            NotificacaoService notificacaoService,
            @Value("${app.inscricoes.dias-sem-contato:3}") int diasSemContato) {
        this.notificacaoService = notificacaoService;
        this.diasSemContato = diasSemContato;
    }

    @Scheduled(initialDelayString = "PT1M", fixedDelayString = "PT1H")
    public void verificarInscricoesSemContato() {
        int criados = notificacaoService.verificarInscricoesSemContato(diasSemContato);
        if (criados > 0) {
            LOG.info("{} aviso(s) de inscrição sem contato criados.", criados);
        }
    }
}
