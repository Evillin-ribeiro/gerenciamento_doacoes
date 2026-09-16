package br.edu.uninter.gestaodoacoes.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public record AgendamentoResponseDTO(
        Long id,
        Long doacaoId,
        Long horarioAtendimentoId,
        LocalDate dataEntrega,
        LocalTime horaInicio,
        LocalTime horaFim
) {
}
