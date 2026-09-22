package com.ibmec.mulheresvalentes.controller.api;

import com.ibmec.mulheresvalentes.dto.api.ParticipanteRequest;
import com.ibmec.mulheresvalentes.dto.api.ParticipanteResponse;
import com.ibmec.mulheresvalentes.model.Participante;
import com.ibmec.mulheresvalentes.service.ParticipanteService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/participantes")
public class ParticipanteRestController {

    private final ParticipanteService participanteService;

    public ParticipanteRestController(ParticipanteService participanteService) {
        this.participanteService = participanteService;
    }

    @GetMapping
    public List<ParticipanteResponse> listar(@RequestParam(required = false) String busca) {
        return participanteService.listar(busca).stream().map(ParticipanteResponse::de).toList();
    }

    @PostMapping
    public ResponseEntity<ParticipanteResponse> cadastrar(@Valid @RequestBody ParticipanteRequest request) {
        Participante participante = participanteService.cadastrar(
                request.nome(), request.contato(), request.observacoes());
        URI localizacao = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(participante.getId())
                .toUri();
        return ResponseEntity.created(localizacao).body(ParticipanteResponse.de(participante));
    }

    @GetMapping("/{id}")
    public ParticipanteResponse buscar(@PathVariable Long id) {
        return ParticipanteResponse.de(participanteService.buscar(id));
    }

    @PutMapping("/{id}")
    public ParticipanteResponse atualizar(
            @PathVariable Long id,
            @Valid @RequestBody ParticipanteRequest request
    ) {
        return ParticipanteResponse.de(
                participanteService.atualizar(id, request.nome(), request.contato(), request.observacoes())
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> desativar(@PathVariable Long id) {
        participanteService.desativar(id);
        return ResponseEntity.noContent().build();
    }
}
