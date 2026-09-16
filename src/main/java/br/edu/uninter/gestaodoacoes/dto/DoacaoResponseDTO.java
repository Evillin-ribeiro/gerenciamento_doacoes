package br.edu.uninter.gestaodoacoes.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import br.edu.uninter.gestaodoacoes.model.StatusDoacao;
import br.edu.uninter.gestaodoacoes.model.TipoDoacao;

public record DoacaoResponseDTO(
        Long id,
        Long doadorId,
        TipoDoacao tipo,
        StatusDoacao status,
        LocalDateTime dataCriacao,
        List<ItemDoacaoResponseDTO> itens,
        BigDecimal valor,
        String comprovanteUrl
) {
}
