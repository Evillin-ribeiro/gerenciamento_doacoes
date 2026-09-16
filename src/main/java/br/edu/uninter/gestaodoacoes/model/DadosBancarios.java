package br.edu.uninter.gestaodoacoes.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Registro unico (id fixo = 1) com os dados bancarios/chave Pix da associacao,
 * exibidos ao doador na doacao financeira (RF06) e editaveis pelo admin.
 */
@Entity
@Table(name = "dados_bancarios")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DadosBancarios {

    public static final Long ID_UNICO = 1L;

    @Id
    private Long id;

    @NotBlank
    private String banco;

    @NotBlank
    private String agencia;

    @NotBlank
    private String conta;

    @NotBlank
    private String chavePix;

    @NotBlank
    private String titular;
}
