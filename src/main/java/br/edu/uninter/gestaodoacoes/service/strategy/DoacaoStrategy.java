package br.edu.uninter.gestaodoacoes.service.strategy;

import java.util.Set;

import br.edu.uninter.gestaodoacoes.dto.DoacaoRequestDTO;
import br.edu.uninter.gestaodoacoes.model.Doacao;
import br.edu.uninter.gestaodoacoes.model.TipoDoacao;

public interface DoacaoStrategy {

    Set<TipoDoacao> getTiposSuportados();

    void processar(Doacao doacao, DoacaoRequestDTO request);
}
