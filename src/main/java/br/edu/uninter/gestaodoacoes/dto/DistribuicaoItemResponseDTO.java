package br.edu.uninter.gestaodoacoes.dto;

import java.math.BigDecimal;

public record DistribuicaoItemResponseDTO(
        Long estoqueId,
        String descricaoItem,
        String unidadeMedida,
        BigDecimal quantidade
) {
}
