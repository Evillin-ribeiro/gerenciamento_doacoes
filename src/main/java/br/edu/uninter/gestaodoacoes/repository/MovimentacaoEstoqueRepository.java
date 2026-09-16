package br.edu.uninter.gestaodoacoes.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import br.edu.uninter.gestaodoacoes.model.MovimentacaoEstoque;

public interface MovimentacaoEstoqueRepository extends JpaRepository<MovimentacaoEstoque, Long> {

    List<MovimentacaoEstoque> findByReferenciaDistribuicaoId(Long distribuicaoId);

    List<MovimentacaoEstoque> findByDataBetween(LocalDateTime inicio, LocalDateTime fim);
}
