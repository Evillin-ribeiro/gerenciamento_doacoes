package br.edu.uninter.gestaodoacoes.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;

public record EstoqueRequestDTO(
        @NotBlank String descricaoItem,
        @NotBlank String categoria,
        @NotBlank String unidadeMedida,
        BigDecimal quantidadeInicial,
        Long usuarioId
) {
    public EstoqueRequestDTO(String descricaoItem, String categoria, String unidadeMedida) {
        this(descricaoItem, categoria, unidadeMedida, null, null);
    }
}
