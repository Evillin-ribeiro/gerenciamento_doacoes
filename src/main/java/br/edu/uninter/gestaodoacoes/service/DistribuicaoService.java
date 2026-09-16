package br.edu.uninter.gestaodoacoes.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.edu.uninter.gestaodoacoes.dto.DistribuicaoItemRequestDTO;
import br.edu.uninter.gestaodoacoes.dto.DistribuicaoItemResponseDTO;
import br.edu.uninter.gestaodoacoes.dto.DistribuicaoRequestDTO;
import br.edu.uninter.gestaodoacoes.dto.DistribuicaoResponseDTO;
import br.edu.uninter.gestaodoacoes.exception.ResourceNotFoundException;
import br.edu.uninter.gestaodoacoes.model.Distribuicao;
import br.edu.uninter.gestaodoacoes.model.Estoque;
import br.edu.uninter.gestaodoacoes.model.MovimentacaoEstoque;
import br.edu.uninter.gestaodoacoes.model.Usuario;
import br.edu.uninter.gestaodoacoes.repository.DistribuicaoRepository;
import br.edu.uninter.gestaodoacoes.repository.MovimentacaoEstoqueRepository;

@Service
@Transactional
public class DistribuicaoService {

    private final DistribuicaoRepository distribuicaoRepository;
    private final MovimentacaoEstoqueRepository movimentacaoEstoqueRepository;
    private final EstoqueService estoqueService;
    private final UsuarioService usuarioService;

    public DistribuicaoService(DistribuicaoRepository distribuicaoRepository,
                                MovimentacaoEstoqueRepository movimentacaoEstoqueRepository,
                                EstoqueService estoqueService,
                                UsuarioService usuarioService) {
        this.distribuicaoRepository = distribuicaoRepository;
        this.movimentacaoEstoqueRepository = movimentacaoEstoqueRepository;
        this.estoqueService = estoqueService;
        this.usuarioService = usuarioService;
    }

    public DistribuicaoResponseDTO criar(DistribuicaoRequestDTO request) {
        Usuario usuario = usuarioService.buscarEntidadePorId(request.usuarioId());

        Distribuicao distribuicao = Distribuicao.builder()
                .data(LocalDateTime.now())
                .beneficiario(request.beneficiario())
                .observacao(request.observacao())
                .build();
        distribuicao = distribuicaoRepository.save(distribuicao);

        for (DistribuicaoItemRequestDTO itemRequest : request.itens()) {
            Estoque estoque = estoqueService.buscarEntidadePorId(itemRequest.estoqueId());
            estoqueService.registrarSaida(estoque, itemRequest.quantidade(), usuario, distribuicao);
        }

        return toResponseDTO(distribuicao);
    }

    @Transactional(readOnly = true)
    public DistribuicaoResponseDTO buscarPorId(Long id) {
        return toResponseDTO(buscarEntidadePorId(id));
    }

    @Transactional(readOnly = true)
    public List<DistribuicaoResponseDTO> listar() {
        return distribuicaoRepository.findAll().stream()
                .map(this::toResponseDTO)
                .toList();
    }

    private Distribuicao buscarEntidadePorId(Long id) {
        return distribuicaoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Distribuicao nao encontrada: id " + id));
    }

    private DistribuicaoResponseDTO toResponseDTO(Distribuicao distribuicao) {
        List<DistribuicaoItemResponseDTO> itens = movimentacaoEstoqueRepository
                .findByReferenciaDistribuicaoId(distribuicao.getId()).stream()
                .map(this::toItemResponseDTO)
                .toList();

        return new DistribuicaoResponseDTO(
                distribuicao.getId(),
                distribuicao.getData(),
                distribuicao.getBeneficiario(),
                distribuicao.getObservacao(),
                itens
        );
    }

    private DistribuicaoItemResponseDTO toItemResponseDTO(MovimentacaoEstoque movimentacao) {
        Estoque estoque = movimentacao.getEstoque();
        return new DistribuicaoItemResponseDTO(
                estoque.getId(),
                estoque.getDescricaoItem(),
                estoque.getUnidadeMedida(),
                movimentacao.getQuantidade()
        );
    }
}
