package com.ibmec.mulheresvalentes.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Ajuste de bancos criados antes do login.
 *
 * O Hibernate criou a coluna historico_operacoes.modulo como ENUM do H2, com a lista
 * fixa de módulos da época. Com ddl-auto=update ele não altera colunas existentes,
 * então o novo módulo USUARIOS seria recusado. Aqui a coluna vira VARCHAR(30),
 * sem perder nenhum registro. Roda antes de qualquer outra inicialização.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class AjusteBancoConfig implements ApplicationRunner {

    private static final Logger LOG = LoggerFactory.getLogger(AjusteBancoConfig.class);

    private final JdbcTemplate jdbc;

    public AjusteBancoConfig(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void run(ApplicationArguments args) {
        try {
            String tipo = jdbc.query("""
                    select data_type from information_schema.columns
                     where upper(table_name) = 'HISTORICO_OPERACOES'
                       and upper(column_name) = 'MODULO'
                    """, rs -> rs.next() ? rs.getString(1) : null);
            if (tipo != null && tipo.toUpperCase().startsWith("ENUM")) {
                jdbc.execute("alter table historico_operacoes alter column modulo set data type varchar(30)");
                LOG.info("Banco ajustado: historico_operacoes.modulo agora aceita novos módulos (VARCHAR).");
            }
        } catch (DataAccessException ex) {
            LOG.warn("Não foi possível verificar a coluna historico_operacoes.modulo: {}", ex.getMessage());
        }
    }
}
