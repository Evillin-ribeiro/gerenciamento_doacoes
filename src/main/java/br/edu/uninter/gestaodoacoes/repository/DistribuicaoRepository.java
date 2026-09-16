package br.edu.uninter.gestaodoacoes.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import br.edu.uninter.gestaodoacoes.model.Distribuicao;

public interface DistribuicaoRepository extends JpaRepository<Distribuicao, Long> {
}
