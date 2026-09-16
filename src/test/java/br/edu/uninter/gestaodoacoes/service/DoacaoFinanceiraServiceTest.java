package br.edu.uninter.gestaodoacoes.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;

import br.edu.uninter.gestaodoacoes.exception.ResourceNotFoundException;
import br.edu.uninter.gestaodoacoes.model.Doacao;
import br.edu.uninter.gestaodoacoes.model.DoacaoFinanceira;
import br.edu.uninter.gestaodoacoes.repository.DoacaoFinanceiraRepository;

@ExtendWith(MockitoExtension.class)
class DoacaoFinanceiraServiceTest {

    @Mock
    private DoacaoFinanceiraRepository doacaoFinanceiraRepository;
    @Mock
    private ComprovanteStorageService comprovanteStorageService;

    private DoacaoFinanceiraService doacaoFinanceiraService;

    @BeforeEach
    void setUp() {
        doacaoFinanceiraService = new DoacaoFinanceiraService(doacaoFinanceiraRepository, comprovanteStorageService);
    }

    private DoacaoFinanceira criarDoacaoFinanceira(String comprovanteAtual) {
        Doacao doacao = Doacao.builder().id(10L).build();
        return DoacaoFinanceira.builder()
                .id(1L)
                .doacao(doacao)
                .valor(new BigDecimal("100.00"))
                .comprovanteUrl(comprovanteAtual)
                .build();
    }

    @Test
    void deveSalvarComprovanteEExcluirAnteriorQuandoExistia() {
        DoacaoFinanceira doacaoFinanceira = criarDoacaoFinanceira("arquivo-antigo.pdf");
        MockMultipartFile arquivo = new MockMultipartFile("arquivo", "comprovante.pdf", "application/pdf", "conteudo".getBytes());

        when(doacaoFinanceiraRepository.findByDoacaoId(10L)).thenReturn(Optional.of(doacaoFinanceira));
        when(comprovanteStorageService.salvar(arquivo)).thenReturn("novo-arquivo.pdf");

        doacaoFinanceiraService.uploadComprovante(10L, arquivo);

        assertThat(doacaoFinanceira.getComprovanteUrl()).isEqualTo("novo-arquivo.pdf");
        verify(doacaoFinanceiraRepository).save(doacaoFinanceira);
        verify(comprovanteStorageService).excluir("arquivo-antigo.pdf");
    }

    @Test
    void naoDeveTentarExcluirQuandoNaoHaviaComprovanteAnterior() {
        DoacaoFinanceira doacaoFinanceira = criarDoacaoFinanceira(null);
        MockMultipartFile arquivo = new MockMultipartFile("arquivo", "comprovante.pdf", "application/pdf", "conteudo".getBytes());

        when(doacaoFinanceiraRepository.findByDoacaoId(10L)).thenReturn(Optional.of(doacaoFinanceira));
        when(comprovanteStorageService.salvar(arquivo)).thenReturn("novo-arquivo.pdf");

        doacaoFinanceiraService.uploadComprovante(10L, arquivo);

        verify(comprovanteStorageService).excluir(eq(null));
    }

    @Test
    void deveLancarExcecaoAoEnviarComprovanteParaDoacaoInexistente() {
        MockMultipartFile arquivo = new MockMultipartFile("arquivo", "comprovante.pdf", "application/pdf", "conteudo".getBytes());
        when(doacaoFinanceiraRepository.findByDoacaoId(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> doacaoFinanceiraService.uploadComprovante(999L, arquivo))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(comprovanteStorageService, never()).salvar(any());
    }

    @Test
    void deveBaixarComprovanteQuandoExiste() {
        DoacaoFinanceira doacaoFinanceira = criarDoacaoFinanceira("arquivo.pdf");
        Resource resourceMock = mock(Resource.class);

        when(doacaoFinanceiraRepository.findByDoacaoId(10L)).thenReturn(Optional.of(doacaoFinanceira));
        when(comprovanteStorageService.carregar("arquivo.pdf")).thenReturn(resourceMock);

        Resource resultado = doacaoFinanceiraService.baixarComprovante(10L);

        assertThat(resultado).isEqualTo(resourceMock);
    }

    @Test
    void deveLancarExcecaoAoBaixarComprovanteInexistente() {
        DoacaoFinanceira doacaoFinanceira = criarDoacaoFinanceira(null);
        when(doacaoFinanceiraRepository.findByDoacaoId(10L)).thenReturn(Optional.of(doacaoFinanceira));

        assertThatThrownBy(() -> doacaoFinanceiraService.baixarComprovante(10L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Nenhum comprovante");
    }
}
