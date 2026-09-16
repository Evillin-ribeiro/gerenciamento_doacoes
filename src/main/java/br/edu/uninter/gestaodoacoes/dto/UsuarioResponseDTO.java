package br.edu.uninter.gestaodoacoes.dto;

import br.edu.uninter.gestaodoacoes.model.RoleUsuario;

public record UsuarioResponseDTO(
        Long id,
        String nome,
        String email,
        RoleUsuario role
) {
}
