package br.edu.uninter.gestaodoacoes.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record EstoqueEntradaRequestDTO(
        @NotNull Long usuarioId,
        @NotNull @Positive BigDecimal quantidade
) {
}
