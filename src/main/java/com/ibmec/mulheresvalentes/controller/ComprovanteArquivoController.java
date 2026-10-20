package com.ibmec.mulheresvalentes.controller;

import com.ibmec.mulheresvalentes.exception.RegraNegocioException;
import com.ibmec.mulheresvalentes.service.ComprovanteArquivoService;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.nio.charset.StandardCharsets;

/** Foto do comprovante na página do lançamento. Remover é só para ADMIN (ver SegurancaConfig). */
@Controller
@RequestMapping("/caixa/movimentacoes/{id}/comprovante")
public class ComprovanteArquivoController {

    private final ComprovanteArquivoService comprovanteService;

    public ComprovanteArquivoController(ComprovanteArquivoService comprovanteService) {
        this.comprovanteService = comprovanteService;
    }

    @GetMapping
    public ResponseEntity<Resource> abrir(@PathVariable Long id) {
        ComprovanteArquivoService.ArquivoParaEnvio arquivo = comprovanteService.abrir(id);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(arquivo.tipoConteudo()))
                .contentLength(arquivo.tamanho())
                .cacheControl(CacheControl.noStore())
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.inline()
                        .filename(arquivo.nomeOriginal(), StandardCharsets.UTF_8).build().toString())
                .header("X-Content-Type-Options", "nosniff")
                .body(new FileSystemResource(arquivo.caminho()));
    }

    @PostMapping
    public String anexar(
            @PathVariable Long id,
            @RequestParam(name = "arquivo", required = false) MultipartFile arquivo,
            RedirectAttributes redirectAttributes) {
        try {
            comprovanteService.anexar(id, arquivo);
            redirectAttributes.addFlashAttribute("sucesso", "Foto do comprovante guardada.");
        } catch (RegraNegocioException ex) {
            redirectAttributes.addFlashAttribute("erro", ex.getMessage());
        }
        return "redirect:/caixa/movimentacoes/" + id + "#comprovante";
    }

    @PostMapping("/remover")
    public String remover(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        comprovanteService.remover(id);
        redirectAttributes.addFlashAttribute("sucesso", "Foto do comprovante removida.");
        return "redirect:/caixa/movimentacoes/" + id + "#comprovante";
    }
}
