package br.edu.uninter.gestaodoacoes.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record RelatorioMovimentacaoDTO(
        LocalDate periodoInicio,
        LocalDate periodoFim,
        BigDecimal totalArrecadadoFinanceiro,
        List<RelatorioItemEstoqueDTO> itensEstoque
) {
}
