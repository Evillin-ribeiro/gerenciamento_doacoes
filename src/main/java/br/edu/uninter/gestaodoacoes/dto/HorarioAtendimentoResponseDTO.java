package br.edu.uninter.gestaodoacoes.dto;

import java.time.DayOfWeek;
import java.time.LocalTime;

public record HorarioAtendimentoResponseDTO(
        Long id,
        DayOfWeek diaSemana,
        LocalTime horaInicio,
        LocalTime horaFim,
        Integer capacidadeMaxima
) {
}
