package br.edu.uninter.gestaodoacoes.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.edu.uninter.gestaodoacoes.dto.DadosBancariosRequestDTO;
import br.edu.uninter.gestaodoacoes.dto.DadosBancariosResponseDTO;
import br.edu.uninter.gestaodoacoes.service.DadosBancariosService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/dados-bancarios")
public class DadosBancariosController {

    private final DadosBancariosService dadosBancariosService;

    public DadosBancariosController(DadosBancariosService dadosBancariosService) {
        this.dadosBancariosService = dadosBancariosService;
    }

    @GetMapping
    public DadosBancariosResponseDTO buscar() {
        return dadosBancariosService.buscar();
    }

    @PutMapping
    public DadosBancariosResponseDTO atualizar(@Valid @RequestBody DadosBancariosRequestDTO request) {
        return dadosBancariosService.atualizar(request);
    }
}
