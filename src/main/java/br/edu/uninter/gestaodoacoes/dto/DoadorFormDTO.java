package br.edu.uninter.gestaodoacoes.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class DoadorFormDTO {
    private String nome;
    private String email;
    private String telefone;
    private String endereco;
}
