package br.edu.uninter.gestaodoacoes.dto;

import java.math.BigDecimal;

public record EstoqueResponseDTO(
        Long id,
        String descricaoItem,
        String categoria,
        String unidadeMedida,
        BigDecimal quantidadeAtual
) {
}
