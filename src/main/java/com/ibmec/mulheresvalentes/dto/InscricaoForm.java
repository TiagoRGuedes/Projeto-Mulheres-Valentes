package com.ibmec.mulheresvalentes.dto;

import com.ibmec.mulheresvalentes.model.AreaApoio;
import com.ibmec.mulheresvalentes.model.PreferenciaContato;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.LinkedHashSet;
import java.util.Set;

public class InscricaoForm {

    @NotBlank(message = "Escreva o seu nome.")
    @Size(max = 150, message = "O nome deve ter no máximo 150 caracteres.")
    private String nome;

    @NotBlank(message = "Deixe um telefone, WhatsApp ou e-mail para a gente falar com você.")
    @Size(max = 120, message = "O contato deve ter no máximo 120 caracteres.")
    private String contato;

    @NotNull(message = "Escolha como prefere que a gente fale com você.")
    private PreferenciaContato preferenciaContato;

    @Size(max = 200, message = "Use no máximo 200 caracteres.")
    private String contatoSeguro;

    @Size(max = 100, message = "O bairro deve ter no máximo 100 caracteres.")
    private String bairro;

    private Set<AreaApoio> areas = new LinkedHashSet<>();

    @Size(max = 1000, message = "A mensagem deve ter no máximo 1000 caracteres.")
    private String mensagem;

    @AssertTrue(message = "Para enviar, confirme que autoriza a ONG a guardar seus dados e entrar em contato.")
    private boolean consentimento;

    private String site;

    public boolean isSuspeitaDeRobo() {
        return site != null && !site.isBlank();
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getContato() {
        return contato;
    }

    public void setContato(String contato) {
        this.contato = contato;
    }

    public PreferenciaContato getPreferenciaContato() {
        return preferenciaContato;
    }

    public void setPreferenciaContato(PreferenciaContato preferenciaContato) {
        this.preferenciaContato = preferenciaContato;
    }

    public String getContatoSeguro() {
        return contatoSeguro;
    }

    public void setContatoSeguro(String contatoSeguro) {
        this.contatoSeguro = contatoSeguro;
    }

    public String getBairro() {
        return bairro;
    }

    public void setBairro(String bairro) {
        this.bairro = bairro;
    }

    public Set<AreaApoio> getAreas() {
        return areas;
    }

    public void setAreas(Set<AreaApoio> areas) {
        this.areas = areas == null ? new LinkedHashSet<>() : new LinkedHashSet<>(areas);
    }

    public String getMensagem() {
        return mensagem;
    }

    public void setMensagem(String mensagem) {
        this.mensagem = mensagem;
    }

    public boolean isConsentimento() {
        return consentimento;
    }

    public void setConsentimento(boolean consentimento) {
        this.consentimento = consentimento;
    }

    public String getSite() {
        return site;
    }

    public void setSite(String site) {
        this.site = site;
    }
}
