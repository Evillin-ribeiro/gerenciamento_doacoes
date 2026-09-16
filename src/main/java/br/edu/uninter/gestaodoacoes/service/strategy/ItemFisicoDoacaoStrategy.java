package br.edu.uninter.gestaodoacoes.service.strategy;

import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Component;

import br.edu.uninter.gestaodoacoes.dto.DoacaoRequestDTO;
import br.edu.uninter.gestaodoacoes.dto.ItemDoacaoRequestDTO;
import br.edu.uninter.gestaodoacoes.exception.RegraNegocioException;
import br.edu.uninter.gestaodoacoes.exception.ResourceNotFoundException;
import br.edu.uninter.gestaodoacoes.model.Doacao;
import br.edu.uninter.gestaodoacoes.model.Estoque;
import br.edu.uninter.gestaodoacoes.model.ItemDoacao;
import br.edu.uninter.gestaodoacoes.model.TipoDoacao;
import br.edu.uninter.gestaodoacoes.repository.EstoqueRepository;
import br.edu.uninter.gestaodoacoes.repository.ItemDoacaoRepository;

@Component
public class ItemFisicoDoacaoStrategy implements DoacaoStrategy {

    private final ItemDoacaoRepository itemDoacaoRepository;
    private final EstoqueRepository estoqueRepository;

    public ItemFisicoDoacaoStrategy(ItemDoacaoRepository itemDoacaoRepository, EstoqueRepository estoqueRepository) {
        this.itemDoacaoRepository = itemDoacaoRepository;
        this.estoqueRepository = estoqueRepository;
    }

    @Override
    public Set<TipoDoacao> getTiposSuportados() {
        return Set.of(TipoDoacao.ALIMENTO, TipoDoacao.ROUPA, TipoDoacao.ITEM_DIVERSO);
    }

    @Override
    public void processar(Doacao doacao, DoacaoRequestDTO request) {
        List<ItemDoacaoRequestDTO> itens = request.itens();
        if (itens == null || itens.isEmpty()) {
            throw new RegraNegocioException("Doacao do tipo " + doacao.getTipo() + " exige ao menos um item.");
        }

        for (ItemDoacaoRequestDTO itemRequest : itens) {
            Estoque estoque = estoqueRepository.findById(itemRequest.estoqueId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Item de estoque nao encontrado: id " + itemRequest.estoqueId()));

            ItemDoacao item = ItemDoacao.builder()
                    .doacao(doacao)
                    .estoque(estoque)
                    .quantidade(itemRequest.quantidade())
                    .build();

            itemDoacaoRepository.save(item);
        }
    }
}
