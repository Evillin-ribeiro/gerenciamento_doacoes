package br.edu.uninter.gestaodoacoes.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.NotNull;

public record AgendamentoRequestDTO(
        @NotNull Long doacaoId,
        @NotNull Long horarioAtendimentoId,
        @NotNull LocalDate dataEntrega
) {
}
