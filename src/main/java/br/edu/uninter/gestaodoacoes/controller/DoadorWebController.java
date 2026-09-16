package br.edu.uninter.gestaodoacoes.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import br.edu.uninter.gestaodoacoes.dto.DoadorFormDTO;
import br.edu.uninter.gestaodoacoes.dto.DoadorRequestDTO;
import br.edu.uninter.gestaodoacoes.dto.DoadorResponseDTO;
import br.edu.uninter.gestaodoacoes.service.DoadorService;

/**
 * Fluxo publico do doador (telas Thymeleaf), reaproveitando o DoadorService diretamente
 * em vez de chamar a API REST via HTTP.
 */
@Controller
@RequestMapping("/doadores")
public class DoadorWebController {

    private final DoadorService doadorService;

    public DoadorWebController(DoadorService doadorService) {
        this.doadorService = doadorService;
    }

    @GetMapping("/novo")
    public String formulario(Model model) {
        if (!model.containsAttribute("doadorForm")) {
            model.addAttribute("doadorForm", new DoadorFormDTO());
        }
        return "doador-form";
    }

    @PostMapping("/novo")
    public String criar(@ModelAttribute("doadorForm") DoadorFormDTO form, Model model) {
        try {
            DoadorRequestDTO request = new DoadorRequestDTO(form.getNome(), form.getEmail(),
                    form.getTelefone(), form.getEndereco());
            DoadorResponseDTO doador = doadorService.criar(request);
            return "redirect:/doacoes/novo?doadorId=" + doador.id();
        } catch (RuntimeException e) {
            model.addAttribute("erro", "Nao foi possivel cadastrar: " + e.getMessage());
            model.addAttribute("doadorForm", form);
            return "doador-form";
        }
    }
}
