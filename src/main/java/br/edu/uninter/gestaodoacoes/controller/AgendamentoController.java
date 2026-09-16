package br.edu.uninter.gestaodoacoes.controller;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.edu.uninter.gestaodoacoes.dto.AgendamentoRequestDTO;
import br.edu.uninter.gestaodoacoes.dto.AgendamentoResponseDTO;
import br.edu.uninter.gestaodoacoes.dto.DisponibilidadeHorarioDTO;
import br.edu.uninter.gestaodoacoes.service.AgendamentoService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/agendamentos")
public class AgendamentoController {

    private final AgendamentoService agendamentoService;

    public AgendamentoController(AgendamentoService agendamentoService) {
        this.agendamentoService = agendamentoService;
    }

    @PostMapping
    public ResponseEntity<AgendamentoResponseDTO> criar(@Valid @RequestBody AgendamentoRequestDTO request) {
        AgendamentoResponseDTO response = agendamentoService.criar(request);
        return ResponseEntity.created(URI.create("/api/agendamentos/" + response.id())).body(response);
    }

    @GetMapping("/{id}")
    public AgendamentoResponseDTO buscarPorId(@PathVariable Long id) {
        return agendamentoService.buscarPorId(id);
    }

    @GetMapping
    public List<AgendamentoResponseDTO> listar() {
        return agendamentoService.listar();
    }

    @GetMapping("/disponibilidade")
    public List<DisponibilidadeHorarioDTO> consultarDisponibilidade(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data) {
        return agendamentoService.consultarDisponibilidade(data);
    }
}
