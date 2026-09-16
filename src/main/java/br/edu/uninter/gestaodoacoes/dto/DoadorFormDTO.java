package br.edu.uninter.gestaodoacoes.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Classe mutavel (nao record) usada apenas para data-binding de formulario Thymeleaf.
 * Convertida para DoadorRequestDTO antes de chamar o service.
 */
@Getter
@Setter
@NoArgsConstructor
public class DoadorFormDTO {
    private String nome;
    private String email;
    private String telefone;
    private String endereco;
}
