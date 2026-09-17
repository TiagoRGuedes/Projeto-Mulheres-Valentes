package com.ibmec.mulheresvalentes.dto;

import com.ibmec.mulheresvalentes.model.CategoriaFinanceira;
import com.ibmec.mulheresvalentes.model.TipoLancamento;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class CategoriaFinanceiraForm {

    @NotBlank(message = "Informe o nome da categoria.")
    @Size(max = 80, message = "O nome da categoria deve ter no máximo 80 caracteres.")
    private String nome;

    @NotNull(message = "Informe se a categoria é de entrada ou de saída.")
    private TipoLancamento tipo;

    public CategoriaFinanceiraForm() {
    }

    public static CategoriaFinanceiraForm from(CategoriaFinanceira categoria) {
        CategoriaFinanceiraForm form = new CategoriaFinanceiraForm();
        form.setNome(categoria.getNome());
        form.setTipo(categoria.getTipo());
        return form;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public TipoLancamento getTipo() {
        return tipo;
    }

    public void setTipo(TipoLancamento tipo) {
        this.tipo = tipo;
    }
}
