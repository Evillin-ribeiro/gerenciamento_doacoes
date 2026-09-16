package br.edu.uninter.gestaodoacoes.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import br.edu.uninter.gestaodoacoes.model.Doador;

public interface DoadorRepository extends JpaRepository<Doador, Long> {
}
