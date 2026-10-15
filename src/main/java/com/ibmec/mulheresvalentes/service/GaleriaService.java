package com.ibmec.mulheresvalentes.service;

import com.ibmec.mulheresvalentes.dto.FotoGaleriaDTO;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
public class GaleriaService {

    private static final String PASTA = "classpath:static/img/publico/galeria/*";
    private static final Set<String> EXTENSOES = Set.of("jpg", "jpeg", "png", "webp");

    private final PathMatchingResourcePatternResolver resolvedor = new PathMatchingResourcePatternResolver();

    public List<FotoGaleriaDTO> listar() {
        Resource[] arquivos;
        try {
            arquivos = resolvedor.getResources(PASTA);
        } catch (IOException ex) {
            return List.of();
        }
        List<FotoGaleriaDTO> fotos = new ArrayList<>();
        for (Resource arquivo : arquivos) {
            String nome = arquivo.getFilename();
            if (nome == null || nome.startsWith(".")) {
                continue;
            }
            int ponto = nome.lastIndexOf('.');
            if (ponto < 0 || !EXTENSOES.contains(nome.substring(ponto + 1).toLowerCase(Locale.ROOT))) {
                continue;
            }
            fotos.add(new FotoGaleriaDTO("/img/publico/galeria/" + nome, descrever(nome.substring(0, ponto))));
        }
        fotos.sort(Comparator.comparing(FotoGaleriaDTO::caminho));
        return fotos;
    }

    static String descrever(String nomeArquivo) {
        String texto = nomeArquivo.replaceFirst("^\\d+[-_ ]*", "").replaceAll("[-_]+", " ").trim();
        if (texto.isEmpty()) {
            return "Atividade da ONG Mulheres Valentes";
        }
        return Character.toUpperCase(texto.charAt(0)) + texto.substring(1);
    }
}
