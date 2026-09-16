package br.edu.uninter.gestaodoacoes.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.HtmlUtils;

import br.edu.uninter.gestaodoacoes.service.DoacaoFinanceiraService;

/**
 * Fluxo publico de upload do comprovante de doacao financeira (tela Thymeleaf),
 * reaproveitando o DoacaoFinanceiraService diretamente.
 */
@Controller
@RequestMapping("/doacoes/{id}/comprovante")
public class ComprovanteWebController {

    private final DoacaoFinanceiraService doacaoFinanceiraService;

    public ComprovanteWebController(DoacaoFinanceiraService doacaoFinanceiraService) {
        this.doacaoFinanceiraService = doacaoFinanceiraService;
    }

    @GetMapping
    public String formulario(@PathVariable Long id, Model model) {
        model.addAttribute("doacaoId", id);
        return "comprovante-form";
    }

    @PostMapping
    public String enviar(@PathVariable Long id, @RequestParam("arquivo") MultipartFile arquivo, Model model) {
        try {
            doacaoFinanceiraService.uploadComprovante(id, arquivo);
            return "redirect:/doacoes/" + id + "?enviado=true";
        } catch (RuntimeException e) {
            model.addAttribute("erro", "Nao foi possivel enviar o comprovante: " + HtmlUtils.htmlEscape(e.getMessage()));
            model.addAttribute("doacaoId", id);
            return "comprovante-form";
        }
    }
}
