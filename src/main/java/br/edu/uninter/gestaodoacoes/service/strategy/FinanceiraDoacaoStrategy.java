package br.edu.uninter.gestaodoacoes.service.strategy;

import java.util.Set;

import org.springframework.stereotype.Component;

import br.edu.uninter.gestaodoacoes.dto.DoacaoRequestDTO;
import br.edu.uninter.gestaodoacoes.exception.RegraNegocioException;
import br.edu.uninter.gestaodoacoes.model.Doacao;
import br.edu.uninter.gestaodoacoes.model.DoacaoFinanceira;
import br.edu.uninter.gestaodoacoes.model.TipoDoacao;
import br.edu.uninter.gestaodoacoes.repository.DoacaoFinanceiraRepository;

@Component
public class FinanceiraDoacaoStrategy implements DoacaoStrategy {

    private final DoacaoFinanceiraRepository doacaoFinanceiraRepository;

    public FinanceiraDoacaoStrategy(DoacaoFinanceiraRepository doacaoFinanceiraRepository) {
        this.doacaoFinanceiraRepository = doacaoFinanceiraRepository;
    }

    @Override
    public Set<TipoDoacao> getTiposSuportados() {
        return Set.of(TipoDoacao.FINANCEIRA);
    }

    @Override
    public void processar(Doacao doacao, DoacaoRequestDTO request) {
        if (request.valor() == null) {
            throw new RegraNegocioException("Doacao do tipo FINANCEIRA exige o campo 'valor'.");
        }

        DoacaoFinanceira doacaoFinanceira = DoacaoFinanceira.builder()
                .doacao(doacao)
                .valor(request.valor())
                .build();

        doacaoFinanceiraRepository.save(doacaoFinanceira);
    }
}
