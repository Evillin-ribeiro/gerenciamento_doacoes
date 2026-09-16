package br.edu.uninter.gestaodoacoes.service.strategy;

import java.util.Set;

import br.edu.uninter.gestaodoacoes.dto.DoacaoRequestDTO;
import br.edu.uninter.gestaodoacoes.model.Doacao;
import br.edu.uninter.gestaodoacoes.model.TipoDoacao;

/**
 * Cada implementacao processa os dados especificos de um ou mais tipos de doacao
 * (ex.: gravar ItemDoacao para itens fisicos, ou DoacaoFinanceira para doacao em dinheiro),
 * evitando um if/else por tipo no service/controller.
 */
public interface DoacaoStrategy {

    Set<TipoDoacao> getTiposSuportados();

    void processar(Doacao doacao, DoacaoRequestDTO request);
}
