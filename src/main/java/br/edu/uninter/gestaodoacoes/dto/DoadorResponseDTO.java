package br.edu.uninter.gestaodoacoes.dto;

public record DoadorResponseDTO(
        Long id,
        String nome,
        String email,
        String telefone,
        String endereco
) {
}
