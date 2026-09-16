package br.edu.uninter.gestaodoacoes.dto;

public record DadosBancariosResponseDTO(
        String banco,
        String agencia,
        String conta,
        String chavePix,
        String titular
) {
}
