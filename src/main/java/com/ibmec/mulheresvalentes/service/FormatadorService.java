package com.ibmec.mulheresvalentes.service;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import org.springframework.stereotype.Service;

@Service("formatador")
public class FormatadorService {

    private static final Locale BRASIL = Locale.of("pt", "BR");
    private static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy", BRASIL);
    private static final DateTimeFormatter DATA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH:mm", BRASIL);

    public String moeda(BigDecimal valor) {
        BigDecimal seguro = valor == null ? BigDecimal.ZERO : valor;
        return NumberFormat.getCurrencyInstance(BRASIL).format(seguro);
    }

    public String percentual(BigDecimal valor) {
        NumberFormat formato = NumberFormat.getNumberInstance(BRASIL);
        formato.setMinimumFractionDigits(1);
        formato.setMaximumFractionDigits(1);
        return formato.format(valor == null ? BigDecimal.ZERO : valor) + "%";
    }

    public String data(LocalDate valor) {
        return valor == null ? "—" : DATA.format(valor);
    }

    public String dataHora(LocalDateTime valor) {
        return valor == null ? "—" : DATA_HORA.format(valor);
    }

    public String diaRelativo(LocalDate valor) {
        if (valor == null) {
            return "Data não informada";
        }
        LocalDate hoje = LocalDate.now();
        if (valor.equals(hoje)) {
            return "Hoje";
        }
        if (valor.equals(hoje.minusDays(1))) {
            return "Ontem";
        }
        return DATA.format(valor);
    }
}
