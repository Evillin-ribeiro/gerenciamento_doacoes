package br.edu.uninter.gestaodoacoes.controller;

import java.time.LocalDate;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.util.HtmlUtils;

import br.edu.uninter.gestaodoacoes.dto.AgendamentoFormDTO;
import br.edu.uninter.gestaodoacoes.dto.AgendamentoRequestDTO;
import br.edu.uninter.gestaodoacoes.dto.AgendamentoResponseDTO;
import br.edu.uninter.gestaodoacoes.service.AgendamentoService;

/**
 * Fluxo publico de agendamento de entrega presencial (tela Thymeleaf), reaproveitando o
 * AgendamentoService diretamente. A consulta de disponibilidade em si roda via JS chamando
 * a API publica GET /api/agendamentos/disponibilidade.
 */
@Controller
@RequestMapping("/agendamentos")
public class AgendamentoWebController {

    private final AgendamentoService agendamentoService;

    public AgendamentoWebController(AgendamentoService agendamentoService) {
        this.agendamentoService = agendamentoService;
    }

    @GetMapping("/novo")
    public String formulario(@RequestParam Long doacaoId, Model model) {
        if (!model.containsAttribute("agendamentoForm")) {
            AgendamentoFormDTO form = new AgendamentoFormDTO();
            form.setDoacaoId(doacaoId);
            model.addAttribute("agendamentoForm", form);
        }
        return "agendamento-form";
    }

    @PostMapping("/novo")
    public String criar(@ModelAttribute("agendamentoForm") AgendamentoFormDTO form, Model model) {
        try {
            AgendamentoRequestDTO request = new AgendamentoRequestDTO(
                    form.getDoacaoId(), form.getHorarioAtendimentoId(), LocalDate.parse(form.getDataEntrega()));
            AgendamentoResponseDTO agendamento = agendamentoService.criar(request);
            return "redirect:/doacoes/" + agendamento.doacaoId() + "?agendado=true";
        } catch (RuntimeException e) {
            model.addAttribute("erro", "Nao foi possivel agendar: " + HtmlUtils.htmlEscape(e.getMessage()));
            model.addAttribute("agendamentoForm", form);
            return "agendamento-form";
        }
    }
}
