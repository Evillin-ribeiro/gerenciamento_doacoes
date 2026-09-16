package br.edu.uninter.gestaodoacoes.dto;

import jakarta.validation.constraints.NotBlank;

public record EstoqueRequestDTO(
        @NotBlank String descricaoItem,
        @NotBlank String categoria,
        @NotBlank String unidadeMedida
) {
}
