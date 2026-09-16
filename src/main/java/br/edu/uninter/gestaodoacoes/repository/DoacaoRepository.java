package br.edu.uninter.gestaodoacoes.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import br.edu.uninter.gestaodoacoes.model.Doacao;

public interface DoacaoRepository extends JpaRepository<Doacao, Long> {
}
