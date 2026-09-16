package br.edu.uninter.gestaodoacoes.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.edu.uninter.gestaodoacoes.dto.RelatorioItemEstoqueDTO;
import br.edu.uninter.gestaodoacoes.dto.RelatorioMovimentacaoDTO;
import br.edu.uninter.gestaodoacoes.exception.RegraNegocioException;
import br.edu.uninter.gestaodoacoes.model.Estoque;
import br.edu.uninter.gestaodoacoes.model.MovimentacaoEstoque;
import br.edu.uninter.gestaodoacoes.model.TipoMovimentacao;
import br.edu.uninter.gestaodoacoes.repository.DoacaoFinanceiraRepository;
import br.edu.uninter.gestaodoacoes.repository.MovimentacaoEstoqueRepository;

/**
 * Relatorio de prestacao de contas (RF09): entradas/saidas de itens fisicos por item,
 * mais o total arrecadado em doacoes financeiras confirmadas, dentro de um periodo.
 */
@Service
@Transactional(readOnly = true)
public class RelatorioService {

    private final MovimentacaoEstoqueRepository movimentacaoEstoqueRepository;
    private final DoacaoFinanceiraRepository doacaoFinanceiraRepository;

    public RelatorioService(MovimentacaoEstoqueRepository movimentacaoEstoqueRepository,
                             DoacaoFinanceiraRepository doacaoFinanceiraRepository) {
        this.movimentacaoEstoqueRepository = movimentacaoEstoqueRepository;
        this.doacaoFinanceiraRepository = doacaoFinanceiraRepository;
    }

    public RelatorioMovimentacaoDTO gerar(LocalDate periodoInicio, LocalDate periodoFim) {
        if (periodoFim.isBefore(periodoInicio)) {
            throw new RegraNegocioException("periodoFim nao pode ser anterior a periodoInicio.");
        }

        LocalDateTime inicio = periodoInicio.atStartOfDay();
        LocalDateTime fim = periodoFim.atTime(LocalTime.MAX);

        List<MovimentacaoEstoque> movimentacoes = movimentacaoEstoqueRepository.findByDataBetween(inicio, fim);

        Map<Estoque, List<MovimentacaoEstoque>> porItem = movimentacoes.stream()
                .collect(Collectors.groupingBy(MovimentacaoEstoque::getEstoque));

        List<RelatorioItemEstoqueDTO> itensEstoque = porItem.entrySet().stream()
                .map(entry -> toItemDTO(entry.getKey(), entry.getValue()))
                .sorted(Comparator.comparing(RelatorioItemEstoqueDTO::descricaoItem))
                .toList();

        BigDecimal totalArrecadadoFinanceiro = doacaoFinanceiraRepository.findConfirmadasNoPeriodo(inicio, fim)
                .stream()
                .map(df -> df.getValor())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new RelatorioMovimentacaoDTO(periodoInicio, periodoFim, totalArrecadadoFinanceiro, itensEstoque);
    }

    private RelatorioItemEstoqueDTO toItemDTO(Estoque estoque, List<MovimentacaoEstoque> movimentacoes) {
        BigDecimal totalEntradas = somar(movimentacoes, TipoMovimentacao.ENTRADA);
        BigDecimal totalSaidas = somar(movimentacoes, TipoMovimentacao.SAIDA);

        return new RelatorioItemEstoqueDTO(
                estoque.getId(),
                estoque.getDescricaoItem(),
                estoque.getUnidadeMedida(),
                totalEntradas,
                totalSaidas,
                estoque.getQuantidadeAtual()
        );
    }

    private BigDecimal somar(List<MovimentacaoEstoque> movimentacoes, TipoMovimentacao tipo) {
        return movimentacoes.stream()
                .filter(m -> m.getTipo() == tipo)
                .map(MovimentacaoEstoque::getQuantidade)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
