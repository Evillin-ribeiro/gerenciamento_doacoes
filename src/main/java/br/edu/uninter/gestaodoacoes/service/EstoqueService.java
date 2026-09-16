package br.edu.uninter.gestaodoacoes.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.edu.uninter.gestaodoacoes.dto.EstoqueRequestDTO;
import br.edu.uninter.gestaodoacoes.dto.EstoqueResponseDTO;
import br.edu.uninter.gestaodoacoes.exception.RegraNegocioException;
import br.edu.uninter.gestaodoacoes.exception.ResourceNotFoundException;
import br.edu.uninter.gestaodoacoes.model.Distribuicao;
import br.edu.uninter.gestaodoacoes.model.Doacao;
import br.edu.uninter.gestaodoacoes.model.Estoque;
import br.edu.uninter.gestaodoacoes.model.MovimentacaoEstoque;
import br.edu.uninter.gestaodoacoes.model.TipoMovimentacao;
import br.edu.uninter.gestaodoacoes.model.Usuario;
import br.edu.uninter.gestaodoacoes.repository.EstoqueRepository;
import br.edu.uninter.gestaodoacoes.repository.MovimentacaoEstoqueRepository;

@Service
@Transactional
public class EstoqueService {

    private final EstoqueRepository estoqueRepository;
    private final MovimentacaoEstoqueRepository movimentacaoEstoqueRepository;

    public EstoqueService(EstoqueRepository estoqueRepository,
                           MovimentacaoEstoqueRepository movimentacaoEstoqueRepository) {
        this.estoqueRepository = estoqueRepository;
        this.movimentacaoEstoqueRepository = movimentacaoEstoqueRepository;
    }

    public EstoqueResponseDTO criar(EstoqueRequestDTO request) {
        Estoque estoque = Estoque.builder()
                .descricaoItem(request.descricaoItem())
                .categoria(request.categoria())
                .unidadeMedida(request.unidadeMedida())
                .quantidadeAtual(BigDecimal.ZERO)
                .build();

        return toResponseDTO(estoqueRepository.save(estoque));
    }

    public EstoqueResponseDTO obterOuCriarItemDiverso(String descricaoItem) {
        String descricao = descricaoItem.trim();
        Estoque estoque = estoqueRepository.findByDescricaoItemIgnoreCase(descricao)
                .orElseGet(() -> estoqueRepository.save(Estoque.builder()
                        .descricaoItem(descricao)
                        .categoria("ITEM_DIVERSO")
                        .unidadeMedida("unidade")
                        .quantidadeAtual(BigDecimal.ZERO)
                        .build()));

        return toResponseDTO(estoque);
    }

    @Transactional(readOnly = true)
    public EstoqueResponseDTO buscarPorId(Long id) {
        return toResponseDTO(buscarEntidadePorId(id));
    }

    @Transactional(readOnly = true)
    public List<EstoqueResponseDTO> listar() {
        return estoqueRepository.findAll().stream()
                .map(this::toResponseDTO)
                .toList();
    }

    public EstoqueResponseDTO atualizar(Long id, EstoqueRequestDTO request) {
        Estoque estoque = buscarEntidadePorId(id);
        estoque.setDescricaoItem(request.descricaoItem());
        estoque.setCategoria(request.categoria());
        estoque.setUnidadeMedida(request.unidadeMedida());

        return toResponseDTO(estoqueRepository.save(estoque));
    }

    Estoque buscarEntidadePorId(Long id) {
        return estoqueRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Item de estoque nao encontrado: id " + id));
    }

    /**
     * Da entrada de um item fisico doado, atualizando o saldo e registrando a movimentacao (RF07).
     */
    void registrarEntrada(Estoque estoque, BigDecimal quantidade, Usuario usuario, Doacao doacaoReferencia) {
        estoque.setQuantidadeAtual(estoque.getQuantidadeAtual().add(quantidade));
        estoqueRepository.save(estoque);

        MovimentacaoEstoque movimentacao = MovimentacaoEstoque.builder()
                .estoque(estoque)
                .usuario(usuario)
                .tipo(TipoMovimentacao.ENTRADA)
                .quantidade(quantidade)
                .data(LocalDateTime.now())
                .referenciaDoacao(doacaoReferencia)
                .build();

        movimentacaoEstoqueRepository.save(movimentacao);
    }

    /**
     * Registra a saida de um item distribuido a um beneficiario, validando saldo disponivel (RF08).
     */
    void registrarSaida(Estoque estoque, BigDecimal quantidade, Usuario usuario, Distribuicao distribuicaoReferencia) {
        if (estoque.getQuantidadeAtual().compareTo(quantidade) < 0) {
            throw new RegraNegocioException("Estoque insuficiente para '" + estoque.getDescricaoItem()
                    + "': disponivel " + estoque.getQuantidadeAtual() + ", solicitado " + quantidade + ".");
        }

        estoque.setQuantidadeAtual(estoque.getQuantidadeAtual().subtract(quantidade));
        estoqueRepository.save(estoque);

        MovimentacaoEstoque movimentacao = MovimentacaoEstoque.builder()
                .estoque(estoque)
                .usuario(usuario)
                .tipo(TipoMovimentacao.SAIDA)
                .quantidade(quantidade)
                .data(LocalDateTime.now())
                .referenciaDistribuicao(distribuicaoReferencia)
                .build();

        movimentacaoEstoqueRepository.save(movimentacao);
    }

    private EstoqueResponseDTO toResponseDTO(Estoque estoque) {
        return new EstoqueResponseDTO(
                estoque.getId(),
                estoque.getDescricaoItem(),
                estoque.getCategoria(),
                estoque.getUnidadeMedida(),
                estoque.getQuantidadeAtual()
        );
    }
}
