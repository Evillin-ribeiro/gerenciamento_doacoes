package br.edu.uninter.gestaodoacoes.controller;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.edu.uninter.gestaodoacoes.dto.DistribuicaoRequestDTO;
import br.edu.uninter.gestaodoacoes.dto.DistribuicaoResponseDTO;
import br.edu.uninter.gestaodoacoes.service.DistribuicaoService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/distribuicoes")
public class DistribuicaoController {

    private final DistribuicaoService distribuicaoService;

    public DistribuicaoController(DistribuicaoService distribuicaoService) {
        this.distribuicaoService = distribuicaoService;
    }

    @PostMapping
    public ResponseEntity<DistribuicaoResponseDTO> criar(@Valid @RequestBody DistribuicaoRequestDTO request) {
        DistribuicaoResponseDTO response = distribuicaoService.criar(request);
        return ResponseEntity.created(URI.create("/api/distribuicoes/" + response.id())).body(response);
    }

    @GetMapping("/{id}")
    public DistribuicaoResponseDTO buscarPorId(@PathVariable Long id) {
        return distribuicaoService.buscarPorId(id);
    }

    @GetMapping
    public List<DistribuicaoResponseDTO> listar() {
        return distribuicaoService.listar();
    }
}
