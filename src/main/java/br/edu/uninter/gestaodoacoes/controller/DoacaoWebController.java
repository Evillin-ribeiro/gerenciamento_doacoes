package br.edu.uninter.gestaodoacoes.controller;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.util.HtmlUtils;

import br.edu.uninter.gestaodoacoes.dto.AgendamentoResponseDTO;
import br.edu.uninter.gestaodoacoes.dto.DadosBancariosResponseDTO;
import br.edu.uninter.gestaodoacoes.dto.DoacaoFormDTO;
import br.edu.uninter.gestaodoacoes.dto.DoacaoRequestDTO;
import br.edu.uninter.gestaodoacoes.dto.DoacaoResponseDTO;
import br.edu.uninter.gestaodoacoes.dto.EstoqueResponseDTO;
import br.edu.uninter.gestaodoacoes.dto.ItemDoacaoRequestDTO;
import br.edu.uninter.gestaodoacoes.model.TipoDoacao;
import br.edu.uninter.gestaodoacoes.service.AgendamentoService;
import br.edu.uninter.gestaodoacoes.service.DadosBancariosService;
import br.edu.uninter.gestaodoacoes.service.DoacaoService;
import br.edu.uninter.gestaodoacoes.service.EstoqueService;

/**
 * Fluxo publico de criacao/consulta de doacao (telas Thymeleaf), reaproveitando os services
 * diretamente em vez de chamar a API REST via HTTP.
 */
@Controller
@RequestMapping("/doacoes")
public class DoacaoWebController {

    private final DoacaoService doacaoService;
    private final EstoqueService estoqueService;
    private final DadosBancariosService dadosBancariosService;
    private final AgendamentoService agendamentoService;

    public DoacaoWebController(DoacaoService doacaoService, EstoqueService estoqueService,
                                DadosBancariosService dadosBancariosService, AgendamentoService agendamentoService) {
        this.doacaoService = doacaoService;
        this.estoqueService = estoqueService;
        this.dadosBancariosService = dadosBancariosService;
        this.agendamentoService = agendamentoService;
    }

    @GetMapping("/novo")
    public String formulario(@RequestParam Long doadorId, Model model) {
        if (!model.containsAttribute("doacaoForm")) {
            DoacaoFormDTO form = new DoacaoFormDTO();
            form.setDoadorId(doadorId);
            model.addAttribute("doacaoForm", form);
        }
        model.addAttribute("itensEstoque", estoqueService.listar());
        return "doacao-form";
    }

    @PostMapping("/novo")
    public String criar(@ModelAttribute("doacaoForm") DoacaoFormDTO form, Model model) {
        try {
            TipoDoacao tipo = TipoDoacao.valueOf(form.getTipo());
            List<ItemDoacaoRequestDTO> itens = null;
            BigDecimal valor = null;

            if (tipo == TipoDoacao.FINANCEIRA) {
                valor = parseDecimal(form.getValor());
            } else {
                itens = new ArrayList<>();
                adicionarSeValido(itens, form.getEstoqueId1(), form.getQuantidade1());
                adicionarSeValido(itens, form.getEstoqueId2(), form.getQuantidade2());
                adicionarSeValido(itens, form.getEstoqueId3(), form.getQuantidade3());
                adicionarOutroSeValido(itens, form.getOutroItemNome(), form.getOutroItemQuantidade());
            }

            DoacaoRequestDTO request = new DoacaoRequestDTO(form.getDoadorId(), tipo, itens, valor);
            DoacaoResponseDTO doacao = doacaoService.criar(request);
            return "redirect:/doacoes/" + doacao.id();
        } catch (RuntimeException e) {
            model.addAttribute("erro", "Nao foi possivel registrar a doacao: " + HtmlUtils.htmlEscape(e.getMessage()));
            model.addAttribute("itensEstoque", estoqueService.listar());
            model.addAttribute("doacaoForm", form);
            return "doacao-form";
        }
    }

    @GetMapping("/{id}")
    public String detalhe(@PathVariable Long id, Model model) {
        DoacaoResponseDTO doacao = doacaoService.buscarPorId(id);
        model.addAttribute("doacao", doacao);

        if (doacao.tipo() == TipoDoacao.FINANCEIRA) {
            DadosBancariosResponseDTO dadosBancarios = dadosBancariosService.buscar();
            model.addAttribute("dadosBancarios", dadosBancarios);
        } else {
            AgendamentoResponseDTO agendamento = agendamentoService.buscarPorDoacaoId(id).orElse(null);
            model.addAttribute("agendamento", agendamento);
        }

        return "doacao-detalhe";
    }

    private void adicionarSeValido(List<ItemDoacaoRequestDTO> itens, Long estoqueId, String quantidadeTexto) {
        if (estoqueId == null) {
            return;
        }
        BigDecimal quantidade = parseDecimal(quantidadeTexto);
        if (quantidade == null || quantidade.signum() <= 0) {
            return;
        }
        itens.add(new ItemDoacaoRequestDTO(estoqueId, quantidade));
    }

    private void adicionarOutroSeValido(List<ItemDoacaoRequestDTO> itens, String nome, String quantidadeTexto) {
        if (nome == null || nome.isBlank()) {
            return;
        }
        BigDecimal quantidade = parseDecimal(quantidadeTexto);
        if (quantidade == null || quantidade.signum() <= 0) {
            return;
        }
        EstoqueResponseDTO item = estoqueService.obterOuCriarItemDiverso(nome);
        itens.add(new ItemDoacaoRequestDTO(item.id(), quantidade));
    }

    private BigDecimal parseDecimal(String texto) {
        if (texto == null || texto.isBlank()) {
            return null;
        }
        return new BigDecimal(texto.trim().replace(",", "."));
    }
}
