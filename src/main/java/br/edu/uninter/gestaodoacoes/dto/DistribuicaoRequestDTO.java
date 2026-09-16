package br.edu.uninter.gestaodoacoes.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record DistribuicaoRequestDTO(
        @NotNull Long usuarioId,
        @NotBlank String beneficiario,
        String observacao,
        @NotEmpty @Valid List<DistribuicaoItemRequestDTO> itens
) {
}
