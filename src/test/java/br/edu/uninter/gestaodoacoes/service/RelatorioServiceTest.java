package br.edu.uninter.gestaodoacoes.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import br.edu.uninter.gestaodoacoes.dto.RelatorioItemEstoqueDTO;
import br.edu.uninter.gestaodoacoes.dto.RelatorioMovimentacaoDTO;
import br.edu.uninter.gestaodoacoes.exception.RegraNegocioException;
import br.edu.uninter.gestaodoacoes.model.Doacao;
import br.edu.uninter.gestaodoacoes.model.DoacaoFinanceira;
import br.edu.uninter.gestaodoacoes.model.Estoque;
import br.edu.uninter.gestaodoacoes.model.MovimentacaoEstoque;
import br.edu.uninter.gestaodoacoes.model.TipoMovimentacao;
import br.edu.uninter.gestaodoacoes.repository.DoacaoFinanceiraRepository;
import br.edu.uninter.gestaodoacoes.repository.MovimentacaoEstoqueRepository;

@ExtendWith(MockitoExtension.class)
class RelatorioServiceTest {

    @Mock
    private MovimentacaoEstoqueRepository movimentacaoEstoqueRepository;
    @Mock
    private DoacaoFinanceiraRepository doacaoFinanceiraRepository;

    private RelatorioService relatorioService;

    @BeforeEach
    void setUp() {
        relatorioService = new RelatorioService(movimentacaoEstoqueRepository, doacaoFinanceiraRepository);
    }

    @Test
    void deveAgruparEntradasESaidasPorItemESomarFinanceiro() {
        Estoque arroz = Estoque.builder().id(1L).descricaoItem("Arroz").unidadeMedida("kg")
                .quantidadeAtual(new BigDecimal("12")).build();

        List<MovimentacaoEstoque> movimentacoes = List.of(
                MovimentacaoEstoque.builder().id(1L).estoque(arroz).tipo(TipoMovimentacao.ENTRADA)
                        .quantidade(new BigDecimal("10")).build(),
                MovimentacaoEstoque.builder().id(2L).estoque(arroz).tipo(TipoMovimentacao.ENTRADA)
                        .quantidade(new BigDecimal("5")).build(),
                MovimentacaoEstoque.builder().id(3L).estoque(arroz).tipo(TipoMovimentacao.SAIDA)
                        .quantidade(new BigDecimal("3")).build()
        );

        when(movimentacaoEstoqueRepository.findByDataBetween(any(), any())).thenReturn(movimentacoes);

        DoacaoFinanceira doacaoFinanceira1 = DoacaoFinanceira.builder().id(1L).valor(new BigDecimal("100.00"))
                .doacao(Doacao.builder().id(1L).build()).build();
        DoacaoFinanceira doacaoFinanceira2 = DoacaoFinanceira.builder().id(2L).valor(new BigDecimal("50.50"))
                .doacao(Doacao.builder().id(2L).build()).build();
        when(doacaoFinanceiraRepository.findConfirmadasNoPeriodo(any(), any()))
                .thenReturn(List.of(doacaoFinanceira1, doacaoFinanceira2));

        RelatorioMovimentacaoDTO relatorio = relatorioService.gerar(
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31));

        assertThat(relatorio.totalArrecadadoFinanceiro()).isEqualByComparingTo("150.50");
        assertThat(relatorio.itensEstoque()).hasSize(1);

        RelatorioItemEstoqueDTO itemArroz = relatorio.itensEstoque().get(0);
        assertThat(itemArroz.totalEntradas()).isEqualByComparingTo("15");
        assertThat(itemArroz.totalSaidas()).isEqualByComparingTo("3");
        assertThat(itemArroz.saldoAtual()).isEqualByComparingTo("12");
    }

    @Test
    void deveRecusarPeriodoInvalido() {
        assertThatThrownBy(() -> relatorioService.gerar(LocalDate.of(2026, 2, 1), LocalDate.of(2026, 1, 1)))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("periodoFim");
    }
}
