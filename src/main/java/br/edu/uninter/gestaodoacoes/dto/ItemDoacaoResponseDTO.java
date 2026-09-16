package br.edu.uninter.gestaodoacoes.dto;

import java.math.BigDecimal;

public record ItemDoacaoResponseDTO(
        Long id,
        Long estoqueId,
        String descricaoItem,
        String unidadeMedida,
        BigDecimal quantidade
) {
}
