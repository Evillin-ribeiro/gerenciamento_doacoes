package br.edu.uninter.gestaodoacoes.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Serve apenas o "shell" HTML do painel administrativo. A protecao real e a busca de dados
 * acontecem via JS (static/js/admin.js) chamando a API REST autenticada com o token JWT.
 */
@Controller
public class AdminWebController {

    @GetMapping("/admin/login")
    public String login() {
        return "admin-login";
    }

    @GetMapping("/admin")
    public String dashboard() {
        return "admin";
    }
}
