package br.edu.uninter.gestaodoacoes.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record DistribuicaoItemRequestDTO(
        @NotNull Long estoqueId,
        @NotNull @Positive BigDecimal quantidade
) {
}
