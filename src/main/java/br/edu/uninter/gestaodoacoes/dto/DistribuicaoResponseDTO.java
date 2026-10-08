package br.edu.uninter.gestaodoacoes.dto;

import java.time.LocalDateTime;
import java.util.List;

public record DistribuicaoResponseDTO(
        Long id,
        LocalDateTime data,
        String beneficiario,
        String voluntarioNome,
        String observacao,
        List<DistribuicaoItemResponseDTO> itens
) {
}
