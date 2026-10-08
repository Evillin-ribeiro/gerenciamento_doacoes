package br.edu.uninter.gestaodoacoes.dto;

import java.math.BigDecimal;
import java.util.List;

import br.edu.uninter.gestaodoacoes.model.TipoDoacao;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record DoacaoRequestDTO(
        @NotNull Long doadorId,
        @NotNull TipoDoacao tipo,
        @Valid List<ItemDoacaoRequestDTO> itens,
        @Positive BigDecimal valor
) {
}
