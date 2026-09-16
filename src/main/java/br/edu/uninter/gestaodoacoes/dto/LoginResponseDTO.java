package br.edu.uninter.gestaodoacoes.dto;

import br.edu.uninter.gestaodoacoes.model.RoleUsuario;

public record LoginResponseDTO(
        String token,
        String tipo,
        Long usuarioId,
        String nome,
        RoleUsuario role
) {
    public LoginResponseDTO(String token, Long usuarioId, String nome, RoleUsuario role) {
        this(token, "Bearer", usuarioId, nome, role);
    }
}
