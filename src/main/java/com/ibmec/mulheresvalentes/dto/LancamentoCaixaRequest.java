package com.ibmec.mulheresvalentes.dto;

import com.ibmec.mulheresvalentes.model.NaturezaLancamento;
import com.ibmec.mulheresvalentes.model.TipoLancamento;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record LancamentoCaixaRequest(
        @NotNull(message = "Informe a data do lançamento.") LocalDate data,
        @NotNull(message = "Informe se o lançamento é uma entrada ou saída.") TipoLancamento tipo,
        NaturezaLancamento natureza,
        @NotNull(message = "Escolha uma categoria.") Long categoriaId,
        @NotBlank(message = "Informe uma descrição.")
        @Size(max = 160, message = "A descrição deve ter no máximo 160 caracteres.") String descricao,
        @DecimalMin(value = "0.00", inclusive = false, message = "Informe um valor maior que zero.")
        @Digits(integer = 8, fraction = 2, message = "Use no máximo 8 dígitos e 2 casas decimais.") BigDecimal valor,
        @Size(max = 200, message = "O comprovante deve ter no máximo 200 caracteres.") String comprovante,
        @Size(max = 500, message = "A observação deve ter no máximo 500 caracteres.") String observacoes) {

    public NaturezaLancamento naturezaOuPadrao() {
        return natureza == null ? NaturezaLancamento.MONETARIA : natureza;
    }
}
