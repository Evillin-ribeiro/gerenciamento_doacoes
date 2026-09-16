package br.edu.uninter.gestaodoacoes.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import br.edu.uninter.gestaodoacoes.model.ItemDoacao;

public interface ItemDoacaoRepository extends JpaRepository<ItemDoacao, Long> {

    List<ItemDoacao> findByDoacaoId(Long doacaoId);
}
