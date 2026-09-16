package br.edu.uninter.gestaodoacoes.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class DoacaoFormDTO {
    private Long doadorId;
    private String tipo;

    private Long estoqueId1;
    private String quantidade1;
    private Long estoqueId2;
    private String quantidade2;
    private Long estoqueId3;
    private String quantidade3;

    private String outroItemNome;
    private String outroItemQuantidade;

    private String valor;
}
