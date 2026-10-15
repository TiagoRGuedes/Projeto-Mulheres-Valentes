package com.ibmec.mulheresvalentes.service;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ConferenciaInscricaoService {

    static final String PENDENTES = "inscricao.conferencias.pendentes";
    static final String CONFIRMADAS = "inscricao.conferencias.confirmadas";

    public enum Resultado {
        LIBERADA,
        JA_ENVIADA,
        DESCONHECIDA
    }

    private final SecureRandom aleatorio = new SecureRandom();

    public String emitir(HttpSession sessao) {
        byte[] bytes = new byte[24];
        aleatorio.nextBytes(bytes);
        String codigo = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        conjunto(sessao, PENDENTES).add(codigo);
        return codigo;
    }

    public Resultado consumir(HttpSession sessao, String codigo) {
        if (codigo == null || codigo.isBlank()) {
            return Resultado.DESCONHECIDA;
        }
        if (conjunto(sessao, PENDENTES).remove(codigo)) {
            return Resultado.LIBERADA;
        }
        return conjunto(sessao, CONFIRMADAS).contains(codigo)
                ? Resultado.JA_ENVIADA
                : Resultado.DESCONHECIDA;
    }

    public void marcarEnviada(HttpSession sessao, String codigo) {
        conjunto(sessao, CONFIRMADAS).add(codigo);
    }

    public void devolver(HttpSession sessao, String codigo) {
        conjunto(sessao, PENDENTES).add(codigo);
    }

    @SuppressWarnings("unchecked")
    private Set<String> conjunto(HttpSession sessao, String chave) {
        synchronized (sessao) {
            Object atual = sessao.getAttribute(chave);
            if (atual instanceof Set<?> existente) {
                return (Set<String>) existente;
            }
            Set<String> novo = ConcurrentHashMap.newKeySet();
            sessao.setAttribute(chave, novo);
            return novo;
        }
    }
}
