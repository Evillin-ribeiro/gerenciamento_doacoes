package br.edu.uninter.gestaodoacoes.dto;

import br.edu.uninter.gestaodoacoes.model.RoleUsuario;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UsuarioRequestDTO(
        @NotBlank String nome,
        @NotBlank @Email String email,
        @NotBlank @Size(min = 6, message = "senha deve ter ao menos 6 caracteres") String senha,
        @NotNull RoleUsuario role
) {
}
