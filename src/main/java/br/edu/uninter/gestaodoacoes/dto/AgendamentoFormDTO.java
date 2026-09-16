package br.edu.uninter.gestaodoacoes.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AgendamentoFormDTO {
    private Long doacaoId;
    private Long horarioAtendimentoId;
    private String dataEntrega;
}
