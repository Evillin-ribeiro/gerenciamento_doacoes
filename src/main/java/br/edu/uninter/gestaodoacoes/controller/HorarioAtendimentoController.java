package br.edu.uninter.gestaodoacoes.controller;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.edu.uninter.gestaodoacoes.dto.HorarioAtendimentoRequestDTO;
import br.edu.uninter.gestaodoacoes.dto.HorarioAtendimentoResponseDTO;
import br.edu.uninter.gestaodoacoes.service.HorarioAtendimentoService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/horarios-atendimento")
public class HorarioAtendimentoController {

    private final HorarioAtendimentoService horarioAtendimentoService;

    public HorarioAtendimentoController(HorarioAtendimentoService horarioAtendimentoService) {
        this.horarioAtendimentoService = horarioAtendimentoService;
    }

    @PostMapping
    public ResponseEntity<HorarioAtendimentoResponseDTO> criar(@Valid @RequestBody HorarioAtendimentoRequestDTO request) {
        HorarioAtendimentoResponseDTO response = horarioAtendimentoService.criar(request);
        return ResponseEntity.created(URI.create("/api/horarios-atendimento/" + response.id())).body(response);
    }

    @GetMapping("/{id}")
    public HorarioAtendimentoResponseDTO buscarPorId(@PathVariable Long id) {
        return horarioAtendimentoService.buscarPorId(id);
    }

    @GetMapping
    public List<HorarioAtendimentoResponseDTO> listar() {
        return horarioAtendimentoService.listar();
    }

    @PutMapping("/{id}")
    public HorarioAtendimentoResponseDTO atualizar(@PathVariable Long id,
                                                     @Valid @RequestBody HorarioAtendimentoRequestDTO request) {
        return horarioAtendimentoService.atualizar(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable Long id) {
        horarioAtendimentoService.remover(id);
        return ResponseEntity.noContent().build();
    }
}
