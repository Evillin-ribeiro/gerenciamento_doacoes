package br.edu.uninter.gestaodoacoes.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import br.edu.uninter.gestaodoacoes.model.Estoque;

public interface EstoqueRepository extends JpaRepository<Estoque, Long> {

    Optional<Estoque> findByDescricaoItemIgnoreCase(String descricaoItem);
}
