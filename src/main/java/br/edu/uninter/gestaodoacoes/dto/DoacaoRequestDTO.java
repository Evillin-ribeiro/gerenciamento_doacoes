package br.edu.uninter.gestaodoacoes.dto;

import java.math.BigDecimal;
import java.util.List;

import br.edu.uninter.gestaodoacoes.model.TipoDoacao;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * itens e obrigatorio para tipo ALIMENTO/ROUPA/ITEM_DIVERSO; valor e obrigatorio para FINANCEIRA.
 * A validacao condicional e feita pela DoacaoStrategy correspondente ao tipo.
 */
public record DoacaoRequestDTO(
        @NotNull Long doadorId,
        @NotNull TipoDoacao tipo,
        @Valid List<ItemDoacaoRequestDTO> itens,
        @Positive BigDecimal valor
) {
}
