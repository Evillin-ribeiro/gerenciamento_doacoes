package br.edu.uninter.gestaodoacoes.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import br.edu.uninter.gestaodoacoes.model.DadosBancarios;

public interface DadosBancariosRepository extends JpaRepository<DadosBancarios, Long> {
}
