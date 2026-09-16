package br.edu.uninter.gestaodoacoes.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import br.edu.uninter.gestaodoacoes.dto.EstoqueResponseDTO;
import br.edu.uninter.gestaodoacoes.exception.RegraNegocioException;
import br.edu.uninter.gestaodoacoes.model.Distribuicao;
import br.edu.uninter.gestaodoacoes.model.Doacao;
import br.edu.uninter.gestaodoacoes.model.Estoque;
import br.edu.uninter.gestaodoacoes.model.MovimentacaoEstoque;
import br.edu.uninter.gestaodoacoes.model.Usuario;
import br.edu.uninter.gestaodoacoes.repository.EstoqueRepository;
import br.edu.uninter.gestaodoacoes.repository.MovimentacaoEstoqueRepository;

@ExtendWith(MockitoExtension.class)
class EstoqueServiceTest {

    @Mock
    private EstoqueRepository estoqueRepository;
    @Mock
    private MovimentacaoEstoqueRepository movimentacaoEstoqueRepository;

    private EstoqueService estoqueService;

    @BeforeEach
    void setUp() {
        estoqueService = new EstoqueService(estoqueRepository, movimentacaoEstoqueRepository);
    }

    @Test
    void deveRegistrarEntradaESomarSaldo() {
        Estoque estoque = Estoque.builder().id(1L).descricaoItem("Arroz").quantidadeAtual(new BigDecimal("10")).build();
        Usuario usuario = Usuario.builder().id(1L).build();
        Doacao doacao = Doacao.builder().id(5L).build();

        estoqueService.registrarEntrada(estoque, new BigDecimal("5"), usuario, doacao);

        assertThat(estoque.getQuantidadeAtual()).isEqualByComparingTo("15");
        verify(estoqueRepository).save(estoque);
        verify(movimentacaoEstoqueRepository).save(any(MovimentacaoEstoque.class));
    }

    @Test
    void deveRegistrarSaidaESubtrairSaldoQuandoHaEstoque() {
        Estoque estoque = Estoque.builder().id(1L).descricaoItem("Arroz").quantidadeAtual(new BigDecimal("10")).build();
        Usuario usuario = Usuario.builder().id(1L).build();
        Distribuicao distribuicao = Distribuicao.builder().id(3L).build();

        estoqueService.registrarSaida(estoque, new BigDecimal("4"), usuario, distribuicao);

        assertThat(estoque.getQuantidadeAtual()).isEqualByComparingTo("6");
        verify(estoqueRepository).save(estoque);
    }

    @Test
    void deveRecusarSaidaQuandoEstoqueInsuficiente() {
        Estoque estoque = Estoque.builder().id(1L).descricaoItem("Arroz").quantidadeAtual(new BigDecimal("2")).build();
        Usuario usuario = Usuario.builder().id(1L).build();
        Distribuicao distribuicao = Distribuicao.builder().id(3L).build();

        assertThatThrownBy(() -> estoqueService.registrarSaida(estoque, new BigDecimal("5"), usuario, distribuicao))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("Estoque insuficiente");

        assertThat(estoque.getQuantidadeAtual()).isEqualByComparingTo("2");
        verify(movimentacaoEstoqueRepository, never()).save(any());
    }

    @Test
    void deveReaproveitarItemDiversoExistenteIgnorandoCapitalizacao() {
        Estoque existente = Estoque.builder().id(9L).descricaoItem("Ventilador").categoria("ITEM_DIVERSO")
                .unidadeMedida("unidade").quantidadeAtual(BigDecimal.ZERO).build();
        when(estoqueRepository.findByDescricaoItemIgnoreCase("ventilador")).thenReturn(Optional.of(existente));

        EstoqueResponseDTO resultado = estoqueService.obterOuCriarItemDiverso(" ventilador ");

        assertThat(resultado.id()).isEqualTo(9L);
        verify(estoqueRepository, never()).save(any());
    }

    @Test
    void deveCriarNovoItemDiversoQuandoNaoEncontradoNoCatalogo() {
        when(estoqueRepository.findByDescricaoItemIgnoreCase("Berço")).thenReturn(Optional.empty());
        ArgumentCaptor<Estoque> captor = ArgumentCaptor.forClass(Estoque.class);
        when(estoqueRepository.save(captor.capture())).thenAnswer(invocation -> {
            Estoque salvo = invocation.getArgument(0);
            salvo.setId(42L);
            return salvo;
        });

        EstoqueResponseDTO resultado = estoqueService.obterOuCriarItemDiverso("Berço");

        assertThat(resultado.id()).isEqualTo(42L);
        assertThat(captor.getValue().getDescricaoItem()).isEqualTo("Berço");
        assertThat(captor.getValue().getCategoria()).isEqualTo("ITEM_DIVERSO");
        assertThat(captor.getValue().getUnidadeMedida()).isEqualTo("unidade");
    }
}
