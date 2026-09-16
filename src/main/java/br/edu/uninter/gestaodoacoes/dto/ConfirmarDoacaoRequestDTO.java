package br.edu.uninter.gestaodoacoes.dto;

import jakarta.validation.constraints.NotNull;

public record ConfirmarDoacaoRequestDTO(
        @NotNull Long usuarioId
) {
}
