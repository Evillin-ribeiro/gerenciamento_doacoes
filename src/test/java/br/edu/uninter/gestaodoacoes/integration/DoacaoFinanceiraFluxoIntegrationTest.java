package br.edu.uninter.gestaodoacoes.integration;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import br.edu.uninter.gestaodoacoes.dto.AgendamentoRequestDTO;
import br.edu.uninter.gestaodoacoes.dto.DoacaoRequestDTO;
import br.edu.uninter.gestaodoacoes.dto.DoadorRequestDTO;
import br.edu.uninter.gestaodoacoes.model.TipoDoacao;

class DoacaoFinanceiraFluxoIntegrationTest extends IntegrationTestBase {

    @Test
    void deveCriarDoacaoFinanceiraEEnviarEBaixarComprovante() throws Exception {
        long doacaoId = criarDoacaoFinanceira(new BigDecimal("150.00"));

        mockMvc.perform(get("/api/doacoes/{id}/comprovante", doacaoId).header("Authorization", bearer(tokenAdmin())))
                .andExpect(status().isNotFound());

        byte[] conteudo = "conteudo-fake-do-comprovante".getBytes();
        MockMultipartFile arquivo = new MockMultipartFile("arquivo", "comprovante.png", "image/png", conteudo);

        mockMvc.perform(multipart("/api/doacoes/{id}/comprovante", doacaoId).file(arquivo))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/doacoes/{id}/comprovante", doacaoId))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/doacoes/{id}/comprovante", doacaoId).header("Authorization", bearer(tokenAdmin())))
                .andExpect(status().isOk())
                .andExpect(result -> {
                    byte[] baixado = result.getResponse().getContentAsByteArray();
                    org.assertj.core.api.Assertions.assertThat(baixado).isEqualTo(conteudo);
                });
    }

    @Test
    void deveRecusarUploadDeTipoDeArquivoNaoPermitido() throws Exception {
        long doacaoId = criarDoacaoFinanceira(new BigDecimal("50.00"));
        MockMultipartFile arquivo = new MockMultipartFile("arquivo", "comprovante.txt", "text/plain", "oi".getBytes());

        mockMvc.perform(multipart("/api/doacoes/{id}/comprovante", doacaoId).file(arquivo))
                .andExpect(status().isUnprocessableContent());
    }

    @Test
    void naoDevePermitirAgendarEntregaPresencialParaDoacaoFinanceira() throws Exception {
        long doacaoId = criarDoacaoFinanceira(new BigDecimal("75.00"));

        AgendamentoRequestDTO request = new AgendamentoRequestDTO(doacaoId, 1L, java.time.LocalDate.now());
        mockMvc.perform(post("/api/agendamentos")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableContent());
    }

    private long criarDoacaoFinanceira(BigDecimal valor) throws Exception {
        String email = "doador-fin-" + System.nanoTime() + "@teste.local";
        DoadorRequestDTO doador = new DoadorRequestDTO("Doador Financeiro", email, "11988887777", null);
        String respostaDoador = mockMvc.perform(post("/api/doadores")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(doador)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        long doadorId = objectMapper.readTree(respostaDoador).get("id").asLong();

        DoacaoRequestDTO request = new DoacaoRequestDTO(doadorId, TipoDoacao.FINANCEIRA, null, valor);
        String respostaDoacao = mockMvc.perform(post("/api/doacoes")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.valor").value(valor.doubleValue()))
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(respostaDoacao).get("id").asLong();
    }
}
