package br.edu.uninter.gestaodoacoes.integration;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;

import br.edu.uninter.gestaodoacoes.dto.LoginRequestDTO;
import br.edu.uninter.gestaodoacoes.dto.UsuarioRequestDTO;
import br.edu.uninter.gestaodoacoes.model.RoleUsuario;

class AutenticacaoIntegrationTest extends IntegrationTestBase {

    @Test
    void deveAutenticarComCredenciaisValidasERetornarToken() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequestDTO(ADMIN_EMAIL, ADMIN_SENHA))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.role").value("ADMIN"));
    }

    @Test
    void deveRecusarLoginComSenhaInvalida() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequestDTO(ADMIN_EMAIL, "senha-errada"))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deveRecusarAcessoAEndpointProtegidoSemToken() throws Exception {
        mockMvc.perform(get("/api/doadores"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deveRecusarVoluntarioEmEndpointSomenteAdmin() throws Exception {
        String tokenVoluntario = criarVoluntarioERetornarToken();

        mockMvc.perform(get("/api/usuarios").header("Authorization", bearer(tokenVoluntario)))
                .andExpect(status().isForbidden());
    }

    @Test
    void devePermitirAdminEmEndpointSomenteAdmin() throws Exception {
        mockMvc.perform(get("/api/usuarios").header("Authorization", bearer(tokenAdmin())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].email").value(ADMIN_EMAIL));
    }

    private String criarVoluntarioERetornarToken() throws Exception {
        String email = "voluntario-" + System.nanoTime() + "@teste.local";
        UsuarioRequestDTO novoVoluntario = new UsuarioRequestDTO("Voluntario Teste", email, "senha123", RoleUsuario.VOLUNTARIO);

        mockMvc.perform(post("/api/usuarios")
                        .header("Authorization", bearer(tokenAdmin()))
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(novoVoluntario)))
                .andExpect(status().isCreated());

        return login(email, "senha123").get("token").asText();
    }
}
