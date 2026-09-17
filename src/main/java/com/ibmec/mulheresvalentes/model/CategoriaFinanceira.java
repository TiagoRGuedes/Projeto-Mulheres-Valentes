package com.ibmec.mulheresvalentes.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity
@Table(
        name = "categorias_financeiras",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_categoria_financeira_nome_tipo",
                columnNames = {"nome", "tipo"}))
public class CategoriaFinanceira {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Informe o nome da categoria.")
    @Size(max = 80, message = "O nome da categoria deve ter no máximo 80 caracteres.")
    @Column(nullable = false, length = 80)
    private String nome;

    @NotNull(message = "Informe se a categoria é de entrada ou de saída.")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private TipoLancamento tipo;

    @Column(nullable = false)
    private boolean ativo = true;

    protected CategoriaFinanceira() {
    }

    public CategoriaFinanceira(String nome, TipoLancamento tipo) {
        this.nome = normalizarNome(nome);
        this.tipo = tipo;
        this.ativo = true;
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = normalizarNome(nome);
    }

    public TipoLancamento getTipo() {
        return tipo;
    }

    public void setTipo(TipoLancamento tipo) {
        this.tipo = tipo;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }

    public void ativar() {
        this.ativo = true;
    }

    public void desativar() {
        this.ativo = false;
    }

    private static String normalizarNome(String nome) {
        return nome == null ? null : nome.trim().replaceAll("\\s+", " ");
    }
}
