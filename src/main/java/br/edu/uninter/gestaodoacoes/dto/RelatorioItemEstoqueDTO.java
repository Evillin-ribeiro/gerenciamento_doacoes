package br.edu.uninter.gestaodoacoes.dto;

import java.math.BigDecimal;

public record RelatorioItemEstoqueDTO(
        Long estoqueId,
        String descricaoItem,
        String unidadeMedida,
        BigDecimal totalEntradas,
        BigDecimal totalSaidas,
        BigDecimal saldoAtual
) {
}
