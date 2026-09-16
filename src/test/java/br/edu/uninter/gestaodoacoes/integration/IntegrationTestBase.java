package br.edu.uninter.gestaodoacoes.integration;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import br.edu.uninter.gestaodoacoes.dto.LoginRequestDTO;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
abstract class IntegrationTestBase {

    protected static final String ADMIN_EMAIL = "admin@example.com";
    protected static final String ADMIN_SENHA = "2026@Admin";

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    protected JsonNode login(String email, String senha) throws Exception {
        String corpo = objectMapper.writeValueAsString(new LoginRequestDTO(email, senha));
        String resposta = mockMvc.perform(post("/api/auth/login").contentType(APPLICATION_JSON).content(corpo))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(resposta);
    }

    protected String tokenAdmin() throws Exception {
        return login(ADMIN_EMAIL, ADMIN_SENHA).get("token").asString();
    }

    protected long usuarioIdAdmin() throws Exception {
        return login(ADMIN_EMAIL, ADMIN_SENHA).get("usuarioId").asLong();
    }

    protected String bearer(String token) {
        return "Bearer " + token;
    }
}
