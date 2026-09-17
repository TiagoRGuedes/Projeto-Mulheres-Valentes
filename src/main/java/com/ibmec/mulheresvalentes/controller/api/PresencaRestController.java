package com.ibmec.mulheresvalentes.controller.api;

import com.ibmec.mulheresvalentes.dto.api.PresencaRequest;
import com.ibmec.mulheresvalentes.dto.api.PresencaResponse;
import com.ibmec.mulheresvalentes.model.Presenca;
import com.ibmec.mulheresvalentes.model.StatusPresenca;
import com.ibmec.mulheresvalentes.service.PresencaService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/presencas")
public class PresencaRestController {

    private final PresencaService presencaService;

    public PresencaRestController(PresencaService presencaService) {
        this.presencaService = presencaService;
    }

    @GetMapping
    public List<PresencaResponse> listar(
            @RequestParam(required = false) Long participanteId,
            @RequestParam(required = false) Long encontroId,
            @RequestParam(required = false) Long atividadeId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicial,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFinal,
            @RequestParam(required = false) StatusPresenca status
    ) {
        return presencaService.listarHistorico(
                        participanteId, encontroId, atividadeId, dataInicial, dataFinal, status, null
                ).stream()
                .map(PresencaResponse::de)
                .toList();
    }

    @GetMapping("/{id}")
    public PresencaResponse buscar(@PathVariable Long id) {
        return PresencaResponse.de(presencaService.buscar(id));
    }

    @PostMapping
    public ResponseEntity<PresencaResponse> registrar(@Valid @RequestBody PresencaRequest request) {
        Presenca presenca = presencaService.registrar(
                request.participanteId(),
                request.encontroId(),
                request.status(),
                request.observacoes()
        );
        URI localizacao = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(presenca.getId())
                .toUri();
        return ResponseEntity.created(localizacao).body(PresencaResponse.de(presenca));
    }
}
