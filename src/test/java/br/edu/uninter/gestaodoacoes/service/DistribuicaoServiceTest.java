package br.edu.uninter.gestaodoacoes.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import br.edu.uninter.gestaodoacoes.dto.DistribuicaoItemRequestDTO;
import br.edu.uninter.gestaodoacoes.dto.DistribuicaoRequestDTO;
import br.edu.uninter.gestaodoacoes.dto.DistribuicaoResponseDTO;
import br.edu.uninter.gestaodoacoes.model.Distribuicao;
import br.edu.uninter.gestaodoacoes.model.Estoque;
import br.edu.uninter.gestaodoacoes.model.MovimentacaoEstoque;
import br.edu.uninter.gestaodoacoes.model.TipoMovimentacao;
import br.edu.uninter.gestaodoacoes.model.Usuario;
import br.edu.uninter.gestaodoacoes.repository.DistribuicaoRepository;
import br.edu.uninter.gestaodoacoes.repository.MovimentacaoEstoqueRepository;

@ExtendWith(MockitoExtension.class)
class DistribuicaoServiceTest {

    @Mock
    private DistribuicaoRepository distribuicaoRepository;
    @Mock
    private MovimentacaoEstoqueRepository movimentacaoEstoqueRepository;
    @Mock
    private EstoqueService estoqueService;
    @Mock
    private UsuarioService usuarioService;

    private DistribuicaoService distribuicaoService;

    @BeforeEach
    void setUp() {
        distribuicaoService = new DistribuicaoService(distribuicaoRepository, movimentacaoEstoqueRepository,
                estoqueService, usuarioService);
    }

    @Test
    void deveCriarDistribuicaoERegistrarSaidaParaCadaItem() {
        Usuario usuario = Usuario.builder().id(1L).build();
        Distribuicao distribuicaoSalva = Distribuicao.builder().id(7L).beneficiario("Familia Silva").build();
        Estoque estoque = Estoque.builder().id(5L).descricaoItem("Arroz").unidadeMedida("kg")
                .quantidadeAtual(new BigDecimal("20")).build();

        DistribuicaoRequestDTO request = new DistribuicaoRequestDTO(1L, "Familia Silva", "Observacao",
                List.of(new DistribuicaoItemRequestDTO(5L, new BigDecimal("3"))));

        when(usuarioService.buscarEntidadePorId(1L)).thenReturn(usuario);
        when(distribuicaoRepository.save(any(Distribuicao.class))).thenReturn(distribuicaoSalva);
        when(estoqueService.buscarEntidadePorId(5L)).thenReturn(estoque);
        when(movimentacaoEstoqueRepository.findByReferenciaDistribuicaoId(7L)).thenReturn(List.of(
                MovimentacaoEstoque.builder().id(50L).estoque(estoque).tipo(TipoMovimentacao.SAIDA)
                        .quantidade(new BigDecimal("3")).build()
        ));

        DistribuicaoResponseDTO response = distribuicaoService.criar(request);

        assertThat(response.beneficiario()).isEqualTo("Familia Silva");
        assertThat(response.itens()).hasSize(1);
        assertThat(response.itens().get(0).quantidade()).isEqualByComparingTo("3");
        verify(estoqueService).registrarSaida(estoque, new BigDecimal("3"), usuario, distribuicaoSalva);
    }
}
