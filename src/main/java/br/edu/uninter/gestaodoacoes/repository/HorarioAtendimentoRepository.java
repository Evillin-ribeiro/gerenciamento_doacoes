package br.edu.uninter.gestaodoacoes.repository;

import java.time.DayOfWeek;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import br.edu.uninter.gestaodoacoes.model.HorarioAtendimento;

public interface HorarioAtendimentoRepository extends JpaRepository<HorarioAtendimento, Long> {

    List<HorarioAtendimento> findByDiaSemana(DayOfWeek diaSemana);
}
