package com.ibmec.mulheresvalentes.dto;

import java.io.Serial;
import java.io.Serializable;
import java.util.Set;

public record PreferenciasAcessibilidade(
        String tamanhoFonte,
        boolean altoContraste,
        boolean modoEscuro,
        boolean reduzirAnimacoes) implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private static final Set<String> TAMANHOS_PERMITIDOS = Set.of("normal", "grande", "muito-grande");

    public PreferenciasAcessibilidade {
        tamanhoFonte = TAMANHOS_PERMITIDOS.contains(tamanhoFonte) ? tamanhoFonte : "normal";
    }

    public static PreferenciasAcessibilidade padrao() {
        return new PreferenciasAcessibilidade("normal", false, false, false);
    }
}
