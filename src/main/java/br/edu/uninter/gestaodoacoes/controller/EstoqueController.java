package br.edu.uninter.gestaodoacoes.controller;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.edu.uninter.gestaodoacoes.dto.EstoqueRequestDTO;
import br.edu.uninter.gestaodoacoes.dto.EstoqueResponseDTO;
import br.edu.uninter.gestaodoacoes.service.EstoqueService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/estoque")
public class EstoqueController {

    private final EstoqueService estoqueService;

    public EstoqueController(EstoqueService estoqueService) {
        this.estoqueService = estoqueService;
    }

    @PostMapping
    public ResponseEntity<EstoqueResponseDTO> criar(@Valid @RequestBody EstoqueRequestDTO request) {
        EstoqueResponseDTO response = estoqueService.criar(request);
        return ResponseEntity.created(URI.create("/api/estoque/" + response.id())).body(response);
    }

    @GetMapping("/{id}")
    public EstoqueResponseDTO buscarPorId(@PathVariable Long id) {
        return estoqueService.buscarPorId(id);
    }

    @GetMapping
    public List<EstoqueResponseDTO> listar() {
        return estoqueService.listar();
    }

    @PutMapping("/{id}")
    public EstoqueResponseDTO atualizar(@PathVariable Long id, @Valid @RequestBody EstoqueRequestDTO request) {
        return estoqueService.atualizar(id, request);
    }
}
