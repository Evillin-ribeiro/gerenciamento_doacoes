package br.edu.uninter.gestaodoacoes.repository;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import br.edu.uninter.gestaodoacoes.model.Agendamento;

public interface AgendamentoRepository extends JpaRepository<Agendamento, Long> {

    Optional<Agendamento> findByDoacaoId(Long doacaoId);

    long countByHorarioAtendimentoIdAndDataEntrega(Long horarioAtendimentoId, LocalDate dataEntrega);
}
