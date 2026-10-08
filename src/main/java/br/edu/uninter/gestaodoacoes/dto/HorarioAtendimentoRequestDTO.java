package br.edu.uninter.gestaodoacoes.dto;

import java.time.DayOfWeek;
import java.time.LocalTime;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record HorarioAtendimentoRequestDTO(
        @NotNull DayOfWeek diaSemana,
        @NotNull LocalTime horaInicio,
        @NotNull LocalTime horaFim,
        @NotNull @Positive Integer capacidadeMaxima,
        Long usuarioId
) {
    public HorarioAtendimentoRequestDTO(DayOfWeek diaSemana, LocalTime horaInicio, LocalTime horaFim,
                                         Integer capacidadeMaxima) {
        this(diaSemana, horaInicio, horaFim, capacidadeMaxima, null);
    }
}
