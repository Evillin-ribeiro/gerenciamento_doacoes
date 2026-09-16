package br.edu.uninter.gestaodoacoes.controller;

import java.net.URI;
import java.util.List;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import br.edu.uninter.gestaodoacoes.dto.ConfirmarDoacaoRequestDTO;
import br.edu.uninter.gestaodoacoes.dto.DoacaoRequestDTO;
import br.edu.uninter.gestaodoacoes.dto.DoacaoResponseDTO;
import br.edu.uninter.gestaodoacoes.service.DoacaoFinanceiraService;
import br.edu.uninter.gestaodoacoes.service.DoacaoService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/doacoes")
public class DoacaoController {

    private final DoacaoService doacaoService;
    private final DoacaoFinanceiraService doacaoFinanceiraService;

    public DoacaoController(DoacaoService doacaoService, DoacaoFinanceiraService doacaoFinanceiraService) {
        this.doacaoService = doacaoService;
        this.doacaoFinanceiraService = doacaoFinanceiraService;
    }

    @PostMapping
    public ResponseEntity<DoacaoResponseDTO> criar(@Valid @RequestBody DoacaoRequestDTO request) {
        DoacaoResponseDTO response = doacaoService.criar(request);
        return ResponseEntity.created(URI.create("/api/doacoes/" + response.id())).body(response);
    }

    @GetMapping("/{id}")
    public DoacaoResponseDTO buscarPorId(@PathVariable Long id) {
        return doacaoService.buscarPorId(id);
    }

    @GetMapping
    public List<DoacaoResponseDTO> listar() {
        return doacaoService.listar();
    }

    @PostMapping("/{id}/confirmar")
    public DoacaoResponseDTO confirmar(@PathVariable Long id, @Valid @RequestBody ConfirmarDoacaoRequestDTO request) {
        return doacaoService.confirmar(id, request);
    }

    @PostMapping("/{id}/cancelar")
    public DoacaoResponseDTO cancelar(@PathVariable Long id) {
        return doacaoService.cancelar(id);
    }

    @PostMapping("/{id}/comprovante")
    public ResponseEntity<Void> enviarComprovante(@PathVariable Long id, @RequestParam("arquivo") MultipartFile arquivo) {
        doacaoFinanceiraService.uploadComprovante(id, arquivo);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/comprovante")
    public ResponseEntity<Resource> baixarComprovante(@PathVariable Long id) {
        Resource arquivo = doacaoFinanceiraService.baixarComprovante(id);
        MediaType contentType = MediaTypeFactory.getMediaType(arquivo).orElse(MediaType.APPLICATION_OCTET_STREAM);
        return ResponseEntity.ok()
                .contentType(contentType)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + arquivo.getFilename() + "\"")
                .body(arquivo);
    }
}
