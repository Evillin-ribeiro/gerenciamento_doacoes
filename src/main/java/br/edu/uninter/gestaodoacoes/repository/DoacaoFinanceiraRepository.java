package br.edu.uninter.gestaodoacoes.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.edu.uninter.gestaodoacoes.model.DoacaoFinanceira;

public interface DoacaoFinanceiraRepository extends JpaRepository<DoacaoFinanceira, Long> {

    Optional<DoacaoFinanceira> findByDoacaoId(Long doacaoId);

    @Query("SELECT df FROM DoacaoFinanceira df WHERE df.doacao.status = 'CONFIRMADA' "
            + "AND df.doacao.dataCriacao BETWEEN :inicio AND :fim")
    List<DoacaoFinanceira> findConfirmadasNoPeriodo(@Param("inicio") LocalDateTime inicio, @Param("fim") LocalDateTime fim);
}
