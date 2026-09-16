package br.edu.uninter.gestaodoacoes.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import br.edu.uninter.gestaodoacoes.dto.ConfirmarDoacaoRequestDTO;
import br.edu.uninter.gestaodoacoes.dto.DoacaoRequestDTO;
import br.edu.uninter.gestaodoacoes.dto.DoacaoResponseDTO;
import br.edu.uninter.gestaodoacoes.dto.ItemDoacaoRequestDTO;
import br.edu.uninter.gestaodoacoes.exception.RegraNegocioException;
import br.edu.uninter.gestaodoacoes.exception.ResourceNotFoundException;
import br.edu.uninter.gestaodoacoes.model.Doacao;
import br.edu.uninter.gestaodoacoes.model.DoacaoFinanceira;
import br.edu.uninter.gestaodoacoes.model.Doador;
import br.edu.uninter.gestaodoacoes.model.Estoque;
import br.edu.uninter.gestaodoacoes.model.ItemDoacao;
import br.edu.uninter.gestaodoacoes.model.StatusDoacao;
import br.edu.uninter.gestaodoacoes.model.TipoDoacao;
import br.edu.uninter.gestaodoacoes.model.Usuario;
import br.edu.uninter.gestaodoacoes.repository.DoacaoFinanceiraRepository;
import br.edu.uninter.gestaodoacoes.repository.DoacaoRepository;
import br.edu.uninter.gestaodoacoes.repository.DoadorRepository;
import br.edu.uninter.gestaodoacoes.repository.EstoqueRepository;
import br.edu.uninter.gestaodoacoes.repository.ItemDoacaoRepository;
import br.edu.uninter.gestaodoacoes.service.strategy.DoacaoStrategy;
import br.edu.uninter.gestaodoacoes.service.strategy.FinanceiraDoacaoStrategy;
import br.edu.uninter.gestaodoacoes.service.strategy.ItemFisicoDoacaoStrategy;

@ExtendWith(MockitoExtension.class)
class DoacaoServiceTest {

    @Mock
    private DoacaoRepository doacaoRepository;
    @Mock
    private DoadorRepository doadorRepository;
    @Mock
    private ItemDoacaoRepository itemDoacaoRepository;
    @Mock
    private DoacaoFinanceiraRepository doacaoFinanceiraRepository;
    @Mock
    private EstoqueRepository estoqueRepository;
    @Mock
    private EstoqueService estoqueService;
    @Mock
    private UsuarioService usuarioService;

    private DoacaoService doacaoService;

    @BeforeEach
    void setUp() {
        List<DoacaoStrategy> estrategias = List.of(
                new ItemFisicoDoacaoStrategy(itemDoacaoRepository, estoqueRepository),
                new FinanceiraDoacaoStrategy(doacaoFinanceiraRepository)
        );
        doacaoService = new DoacaoService(doacaoRepository, doadorRepository, itemDoacaoRepository,
                doacaoFinanceiraRepository, estrategias, estoqueService, usuarioService);
    }

    private Doador criarDoador() {
        return Doador.builder().id(1L).nome("Maria").email("maria@teste.com").telefone("19999999999").build();
    }

    private Doacao criarDoacaoSalva(Doador doador, TipoDoacao tipo) {
        return Doacao.builder()
                .id(10L)
                .doador(doador)
                .tipo(tipo)
                .status(StatusDoacao.PENDENTE)
                .dataCriacao(LocalDateTime.now())
                .build();
    }

    @Test
    void deveCriarDoacaoDeItemFisicoEGravarItens() {
        Doador doador = criarDoador();
        Doacao doacaoSalva = criarDoacaoSalva(doador, TipoDoacao.ALIMENTO);
        Estoque estoque = Estoque.builder().id(5L).descricaoItem("Arroz").categoria("ALIMENTO")
                .unidadeMedida("kg").quantidadeAtual(BigDecimal.ZERO).build();

        DoacaoRequestDTO request = new DoacaoRequestDTO(1L, TipoDoacao.ALIMENTO,
                List.of(new ItemDoacaoRequestDTO(5L, BigDecimal.TEN)), null);

        when(doadorRepository.findById(1L)).thenReturn(Optional.of(doador));
        when(doacaoRepository.save(any(Doacao.class))).thenReturn(doacaoSalva);
        when(estoqueRepository.findById(5L)).thenReturn(Optional.of(estoque));
        when(itemDoacaoRepository.findByDoacaoId(10L)).thenReturn(List.of(
                ItemDoacao.builder().id(100L).doacao(doacaoSalva).estoque(estoque).quantidade(BigDecimal.TEN).build()
        ));

        DoacaoResponseDTO response = doacaoService.criar(request);

        assertThat(response.tipo()).isEqualTo(TipoDoacao.ALIMENTO);
        assertThat(response.status()).isEqualTo(StatusDoacao.PENDENTE);
        assertThat(response.itens()).hasSize(1);
        assertThat(response.itens().get(0).descricaoItem()).isEqualTo("Arroz");
        verify(itemDoacaoRepository).save(any(ItemDoacao.class));
    }

    @Test
    void deveCriarDoacaoFinanceiraEGravarValor() {
        Doador doador = criarDoador();
        Doacao doacaoSalva = criarDoacaoSalva(doador, TipoDoacao.FINANCEIRA);

        DoacaoRequestDTO request = new DoacaoRequestDTO(1L, TipoDoacao.FINANCEIRA, null, new BigDecimal("150.00"));

        when(doadorRepository.findById(1L)).thenReturn(Optional.of(doador));
        when(doacaoRepository.save(any(Doacao.class))).thenReturn(doacaoSalva);
        when(doacaoFinanceiraRepository.findByDoacaoId(10L)).thenReturn(Optional.of(
                DoacaoFinanceira.builder().id(200L).doacao(doacaoSalva).valor(new BigDecimal("150.00")).build()
        ));

        DoacaoResponseDTO response = doacaoService.criar(request);

        assertThat(response.tipo()).isEqualTo(TipoDoacao.FINANCEIRA);
        assertThat(response.valor()).isEqualByComparingTo("150.00");
        assertThat(response.itens()).isNull();
        verify(doacaoFinanceiraRepository).save(any(DoacaoFinanceira.class));
    }

    @Test
    void deveLancarExcecaoQuandoDoadorNaoExiste() {
        DoacaoRequestDTO request = new DoacaoRequestDTO(99L, TipoDoacao.ALIMENTO,
                List.of(new ItemDoacaoRequestDTO(5L, BigDecimal.ONE)), null);

        when(doadorRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> doacaoService.criar(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Doador nao encontrado");
    }

    @Test
    void deveLancarExcecaoQuandoItemFisicoSemItens() {
        Doador doador = criarDoador();
        Doacao doacaoSalva = criarDoacaoSalva(doador, TipoDoacao.ROUPA);
        DoacaoRequestDTO request = new DoacaoRequestDTO(1L, TipoDoacao.ROUPA, List.of(), null);

        when(doadorRepository.findById(1L)).thenReturn(Optional.of(doador));
        when(doacaoRepository.save(any(Doacao.class))).thenReturn(doacaoSalva);

        assertThatThrownBy(() -> doacaoService.criar(request))
                .isInstanceOf(br.edu.uninter.gestaodoacoes.exception.RegraNegocioException.class)
                .hasMessageContaining("exige ao menos um item");
    }

    @Test
    void deveLancarExcecaoQuandoFinanceiraSemValor() {
        Doador doador = criarDoador();
        Doacao doacaoSalva = criarDoacaoSalva(doador, TipoDoacao.FINANCEIRA);
        DoacaoRequestDTO request = new DoacaoRequestDTO(1L, TipoDoacao.FINANCEIRA, null, null);

        when(doadorRepository.findById(1L)).thenReturn(Optional.of(doador));
        when(doacaoRepository.save(any(Doacao.class))).thenReturn(doacaoSalva);

        assertThatThrownBy(() -> doacaoService.criar(request))
                .isInstanceOf(br.edu.uninter.gestaodoacoes.exception.RegraNegocioException.class)
                .hasMessageContaining("exige o campo 'valor'");
    }

    @Test
    void deveLancarExcecaoQuandoDoacaoNaoExisteAoBuscar() {
        when(doacaoRepository.findById(123L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> doacaoService.buscarPorId(123L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Doacao nao encontrada");
    }

    @Test
    void deveConfirmarDoacaoDeItemFisicoEDarEntradaNoEstoque() {
        Doador doador = criarDoador();
        Doacao doacao = criarDoacaoSalva(doador, TipoDoacao.ALIMENTO);
        Usuario usuario = Usuario.builder().id(1L).build();
        Estoque estoque = Estoque.builder().id(5L).descricaoItem("Arroz").quantidadeAtual(BigDecimal.ZERO).build();
        ItemDoacao item = ItemDoacao.builder().id(100L).doacao(doacao).estoque(estoque).quantidade(BigDecimal.TEN).build();

        when(doacaoRepository.findById(10L)).thenReturn(Optional.of(doacao));
        when(usuarioService.buscarEntidadePorId(1L)).thenReturn(usuario);
        when(itemDoacaoRepository.findByDoacaoId(10L)).thenReturn(List.of(item));

        DoacaoResponseDTO response = doacaoService.confirmar(10L, new ConfirmarDoacaoRequestDTO(1L));

        assertThat(response.status()).isEqualTo(StatusDoacao.CONFIRMADA);
        verify(estoqueService).registrarEntrada(estoque, BigDecimal.TEN, usuario, doacao);
        verify(doacaoRepository).save(doacao);
    }

    @Test
    void naoDeveDarEntradaNoEstoquePorDoacaoFinanceiraAoConfirmar() {
        Doador doador = criarDoador();
        Doacao doacao = criarDoacaoSalva(doador, TipoDoacao.FINANCEIRA);
        Usuario usuario = Usuario.builder().id(1L).build();

        when(doacaoRepository.findById(10L)).thenReturn(Optional.of(doacao));
        when(usuarioService.buscarEntidadePorId(1L)).thenReturn(usuario);
        when(doacaoFinanceiraRepository.findByDoacaoId(10L)).thenReturn(Optional.empty());

        doacaoService.confirmar(10L, new ConfirmarDoacaoRequestDTO(1L));

        verify(estoqueService, org.mockito.Mockito.never()).registrarEntrada(any(), any(), any(), any());
    }

    @Test
    void naoDeveConfirmarDoacaoQueNaoEstaPendente() {
        Doador doador = criarDoador();
        Doacao doacao = criarDoacaoSalva(doador, TipoDoacao.ALIMENTO);
        doacao.setStatus(StatusDoacao.CONFIRMADA);

        when(doacaoRepository.findById(10L)).thenReturn(Optional.of(doacao));

        assertThatThrownBy(() -> doacaoService.confirmar(10L, new ConfirmarDoacaoRequestDTO(1L)))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("Apenas doacoes PENDENTE");
    }

    @Test
    void deveCancelarDoacaoPendente() {
        Doador doador = criarDoador();
        Doacao doacao = criarDoacaoSalva(doador, TipoDoacao.ALIMENTO);

        when(doacaoRepository.findById(10L)).thenReturn(Optional.of(doacao));

        DoacaoResponseDTO response = doacaoService.cancelar(10L);

        assertThat(response.status()).isEqualTo(StatusDoacao.CANCELADA);
    }

    @Test
    void naoDeveCancelarDoacaoJaConfirmada() {
        Doador doador = criarDoador();
        Doacao doacao = criarDoacaoSalva(doador, TipoDoacao.ALIMENTO);
        doacao.setStatus(StatusDoacao.CONFIRMADA);

        when(doacaoRepository.findById(10L)).thenReturn(Optional.of(doacao));

        assertThatThrownBy(() -> doacaoService.cancelar(10L))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("Apenas doacoes PENDENTE");
    }
}
