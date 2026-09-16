package br.edu.uninter.gestaodoacoes.dto;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;

public record DisponibilidadeHorarioDTO(
        Long horarioAtendimentoId,
        LocalDate data,
        DayOfWeek diaSemana,
        LocalTime horaInicio,
        LocalTime horaFim,
        int capacidadeMaxima,
        int vagasOcupadas,
        int vagasDisponiveis
) {
}
