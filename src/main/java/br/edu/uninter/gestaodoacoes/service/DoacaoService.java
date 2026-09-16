package br.edu.uninter.gestaodoacoes.service;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.edu.uninter.gestaodoacoes.dto.ConfirmarDoacaoRequestDTO;
import br.edu.uninter.gestaodoacoes.dto.DoacaoRequestDTO;
import br.edu.uninter.gestaodoacoes.dto.DoacaoResponseDTO;
import br.edu.uninter.gestaodoacoes.dto.ItemDoacaoResponseDTO;
import br.edu.uninter.gestaodoacoes.exception.RegraNegocioException;
import br.edu.uninter.gestaodoacoes.exception.ResourceNotFoundException;
import br.edu.uninter.gestaodoacoes.model.Doacao;
import br.edu.uninter.gestaodoacoes.model.DoacaoFinanceira;
import br.edu.uninter.gestaodoacoes.model.Doador;
import br.edu.uninter.gestaodoacoes.model.ItemDoacao;
import br.edu.uninter.gestaodoacoes.model.StatusDoacao;
import br.edu.uninter.gestaodoacoes.model.TipoDoacao;
import br.edu.uninter.gestaodoacoes.model.Usuario;
import br.edu.uninter.gestaodoacoes.repository.DoacaoFinanceiraRepository;
import br.edu.uninter.gestaodoacoes.repository.DoacaoRepository;
import br.edu.uninter.gestaodoacoes.repository.DoadorRepository;
import br.edu.uninter.gestaodoacoes.repository.ItemDoacaoRepository;
import br.edu.uninter.gestaodoacoes.service.strategy.DoacaoStrategy;

@Service
@Transactional
public class DoacaoService {

    private final DoacaoRepository doacaoRepository;
    private final DoadorRepository doadorRepository;
    private final ItemDoacaoRepository itemDoacaoRepository;
    private final DoacaoFinanceiraRepository doacaoFinanceiraRepository;
    private final Map<TipoDoacao, DoacaoStrategy> estrategiasPorTipo;
    private final EstoqueService estoqueService;
    private final UsuarioService usuarioService;

    public DoacaoService(DoacaoRepository doacaoRepository,
                          DoadorRepository doadorRepository,
                          ItemDoacaoRepository itemDoacaoRepository,
                          DoacaoFinanceiraRepository doacaoFinanceiraRepository,
                          List<DoacaoStrategy> estrategias,
                          EstoqueService estoqueService,
                          UsuarioService usuarioService) {
        this.doacaoRepository = doacaoRepository;
        this.doadorRepository = doadorRepository;
        this.itemDoacaoRepository = itemDoacaoRepository;
        this.doacaoFinanceiraRepository = doacaoFinanceiraRepository;
        this.estoqueService = estoqueService;
        this.usuarioService = usuarioService;

        this.estrategiasPorTipo = new EnumMap<>(TipoDoacao.class);
        for (DoacaoStrategy estrategia : estrategias) {
            for (TipoDoacao tipo : estrategia.getTiposSuportados()) {
                estrategiasPorTipo.put(tipo, estrategia);
            }
        }
    }

    public DoacaoResponseDTO criar(DoacaoRequestDTO request) {
        Doador doador = doadorRepository.findById(request.doadorId())
                .orElseThrow(() -> new ResourceNotFoundException("Doador nao encontrado: id " + request.doadorId()));

        Doacao doacao = Doacao.builder()
                .doador(doador)
                .tipo(request.tipo())
                .status(StatusDoacao.PENDENTE)
                .build();
        doacao = doacaoRepository.save(doacao);

        DoacaoStrategy estrategia = estrategiasPorTipo.get(request.tipo());
        estrategia.processar(doacao, request);

        return toResponseDTO(doacao);
    }

    /**
     * Confirma uma doacao PENDENTE. Para itens fisicos, da entrada automatica no estoque (RF07).
     */
    public DoacaoResponseDTO confirmar(Long id, ConfirmarDoacaoRequestDTO request) {
        Doacao doacao = buscarEntidadePorId(id);
        if (doacao.getStatus() != StatusDoacao.PENDENTE) {
            throw new RegraNegocioException("Apenas doacoes PENDENTE podem ser confirmadas (status atual: "
                    + doacao.getStatus() + ").");
        }

        Usuario usuario = usuarioService.buscarEntidadePorId(request.usuarioId());

        if (doacao.getTipo() != TipoDoacao.FINANCEIRA) {
            for (ItemDoacao item : itemDoacaoRepository.findByDoacaoId(doacao.getId())) {
                estoqueService.registrarEntrada(item.getEstoque(), item.getQuantidade(), usuario, doacao);
            }
        }

        doacao.setStatus(StatusDoacao.CONFIRMADA);
        doacaoRepository.save(doacao);

        return toResponseDTO(doacao);
    }

    /**
     * Cancela uma doacao PENDENTE. Nao permitido apos CONFIRMADA para nao exigir estorno de estoque.
     */
    public DoacaoResponseDTO cancelar(Long id) {
        Doacao doacao = buscarEntidadePorId(id);
        if (doacao.getStatus() != StatusDoacao.PENDENTE) {
            throw new RegraNegocioException("Apenas doacoes PENDENTE podem ser canceladas (status atual: "
                    + doacao.getStatus() + ").");
        }

        doacao.setStatus(StatusDoacao.CANCELADA);
        doacaoRepository.save(doacao);

        return toResponseDTO(doacao);
    }

    @Transactional(readOnly = true)
    public DoacaoResponseDTO buscarPorId(Long id) {
        return toResponseDTO(buscarEntidadePorId(id));
    }

    @Transactional(readOnly = true)
    public List<DoacaoResponseDTO> listar() {
        return doacaoRepository.findAll().stream()
                .map(this::toResponseDTO)
                .toList();
    }

    private Doacao buscarEntidadePorId(Long id) {
        return doacaoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Doacao nao encontrada: id " + id));
    }

    private DoacaoResponseDTO toResponseDTO(Doacao doacao) {
        List<ItemDoacaoResponseDTO> itens = null;
        BigDecimal valor = null;
        String comprovanteUrl = null;

        if (doacao.getTipo() == TipoDoacao.FINANCEIRA) {
            DoacaoFinanceira financeira = doacaoFinanceiraRepository.findByDoacaoId(doacao.getId()).orElse(null);
            if (financeira != null) {
                valor = financeira.getValor();
                comprovanteUrl = financeira.getComprovanteUrl();
            }
        } else {
            itens = itemDoacaoRepository.findByDoacaoId(doacao.getId()).stream()
                    .map(this::toItemResponseDTO)
                    .toList();
        }

        return new DoacaoResponseDTO(
                doacao.getId(),
                doacao.getDoador().getId(),
                doacao.getTipo(),
                doacao.getStatus(),
                doacao.getDataCriacao(),
                itens,
                valor,
                comprovanteUrl
        );
    }

    private ItemDoacaoResponseDTO toItemResponseDTO(ItemDoacao item) {
        return new ItemDoacaoResponseDTO(
                item.getId(),
                item.getEstoque().getId(),
                item.getEstoque().getDescricaoItem(),
                item.getEstoque().getUnidadeMedida(),
                item.getQuantidade()
        );
    }
}
