package br.edu.uninter.gestaodoacoes.dto;

import jakarta.validation.constraints.NotBlank;

public record DadosBancariosRequestDTO(
        @NotBlank String banco,
        @NotBlank String agencia,
        @NotBlank String conta,
        @NotBlank String chavePix,
        @NotBlank String titular
) {
}
