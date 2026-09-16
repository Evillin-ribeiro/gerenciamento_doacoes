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

import br.edu.uninter.gestaodoacoes.dto.DoadorRequestDTO;
import br.edu.uninter.gestaodoacoes.dto.DoadorResponseDTO;
import br.edu.uninter.gestaodoacoes.service.DoadorService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/doadores")
public class DoadorController {

    private final DoadorService doadorService;

    public DoadorController(DoadorService doadorService) {
        this.doadorService = doadorService;
    }

    @PostMapping
    public ResponseEntity<DoadorResponseDTO> criar(@Valid @RequestBody DoadorRequestDTO request) {
        DoadorResponseDTO response = doadorService.criar(request);
        return ResponseEntity.created(URI.create("/api/doadores/" + response.id())).body(response);
    }

    @GetMapping("/{id}")
    public DoadorResponseDTO buscarPorId(@PathVariable Long id) {
        return doadorService.buscarPorId(id);
    }

    @GetMapping
    public List<DoadorResponseDTO> listar() {
        return doadorService.listar();
    }

    @PutMapping("/{id}")
    public DoadorResponseDTO atualizar(@PathVariable Long id, @Valid @RequestBody DoadorRequestDTO request) {
        return doadorService.atualizar(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable Long id) {
        doadorService.remover(id);
        return ResponseEntity.noContent().build();
    }
}
