package br.edu.uninter.gestaodoacoes.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;

import br.edu.uninter.gestaodoacoes.dto.ConfirmarDoacaoRequestDTO;
import br.edu.uninter.gestaodoacoes.dto.DoacaoRequestDTO;
import br.edu.uninter.gestaodoacoes.dto.DoadorRequestDTO;
import br.edu.uninter.gestaodoacoes.dto.ItemDoacaoRequestDTO;
import br.edu.uninter.gestaodoacoes.model.TipoDoacao;
import tools.jackson.databind.JsonNode;

class DoacaoItemFisicoFluxoIntegrationTest extends IntegrationTestBase {

    @Test
    void deveCriarEConfirmarDoacaoDeItemFisicoEDarEntradaNoEstoque() throws Exception {
        long doadorId = criarDoador();
        long estoqueIdArroz = buscarIdDoItemNoCatalogo("Arroz");
        BigDecimal quantidadeAntes = quantidadeAtualDoItem(estoqueIdArroz);

        DoacaoRequestDTO request = new DoacaoRequestDTO(doadorId, TipoDoacao.ALIMENTO,
                List.of(new ItemDoacaoRequestDTO(estoqueIdArroz, new BigDecimal("5"))), null);

        String resposta = mockMvc.perform(post("/api/doacoes")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDENTE"))
                .andExpect(jsonPath("$.itens[0].descricaoItem").value("Arroz"))
                .andReturn().getResponse().getContentAsString();
        long doacaoId = objectMapper.readTree(resposta).get("id").asLong();

        mockMvc.perform(get("/api/doacoes/{id}", doacaoId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PENDENTE"));

        mockMvc.perform(post("/api/doacoes/{id}/confirmar", doacaoId)
                        .header("Authorization", bearer(tokenAdmin()))
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ConfirmarDoacaoRequestDTO(usuarioIdAdmin()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONFIRMADA"));

        assertThat(quantidadeAtualDoItem(estoqueIdArroz)).isEqualByComparingTo(quantidadeAntes.add(new BigDecimal("5")));
    }

    @Test
    void naoDevePermitirConfirmarDoacaoJaConfirmada() throws Exception {
        long doacaoId = criarDoacaoAlimentoPendente();
        String token = bearer(tokenAdmin());
        String corpoConfirmar = objectMapper.writeValueAsString(new ConfirmarDoacaoRequestDTO(usuarioIdAdmin()));

        mockMvc.perform(post("/api/doacoes/{id}/confirmar", doacaoId)
                        .header("Authorization", token).contentType(APPLICATION_JSON).content(corpoConfirmar))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/doacoes/{id}/confirmar", doacaoId)
                        .header("Authorization", token).contentType(APPLICATION_JSON).content(corpoConfirmar))
                .andExpect(status().isUnprocessableContent());
    }

    @Test
    void devePermitirCancelarDoacaoPendenteMasNaoUmaJaConfirmada() throws Exception {
        String token = bearer(tokenAdmin());

        long doacaoCancelavelId = criarDoacaoAlimentoPendente();
        mockMvc.perform(post("/api/doacoes/{id}/cancelar", doacaoCancelavelId).header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELADA"));

        long doacaoConfirmadaId = criarDoacaoAlimentoPendente();
        mockMvc.perform(post("/api/doacoes/{id}/confirmar", doacaoConfirmadaId)
                        .header("Authorization", token)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ConfirmarDoacaoRequestDTO(usuarioIdAdmin()))))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/doacoes/{id}/cancelar", doacaoConfirmadaId).header("Authorization", token))
                .andExpect(status().isUnprocessableContent());
    }

    @Test
    void deveExigirAoMenosUmItemParaDoacaoDeItemFisico() throws Exception {
        long doadorId = criarDoador();
        DoacaoRequestDTO request = new DoacaoRequestDTO(doadorId, TipoDoacao.ALIMENTO, List.of(), null);

        mockMvc.perform(post("/api/doacoes")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableContent());
    }

    private long criarDoacaoAlimentoPendente() throws Exception {
        long doadorId = criarDoador();
        long estoqueId = buscarIdDoItemNoCatalogo("Arroz");
        DoacaoRequestDTO request = new DoacaoRequestDTO(doadorId, TipoDoacao.ALIMENTO,
                List.of(new ItemDoacaoRequestDTO(estoqueId, new BigDecimal("1"))), null);

        String resposta = mockMvc.perform(post("/api/doacoes")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(resposta).get("id").asLong();
    }

    private long criarDoador() throws Exception {
        String email = "doador-" + System.nanoTime() + "@teste.local";
        DoadorRequestDTO request = new DoadorRequestDTO("Doador Teste", email, "11999999999", null);

        String resposta = mockMvc.perform(post("/api/doadores")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(resposta).get("id").asLong();
    }

    private long buscarIdDoItemNoCatalogo(String descricao) throws Exception {
        String resposta = mockMvc.perform(get("/api/estoque"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        for (JsonNode item : objectMapper.readTree(resposta)) {
            if (descricao.equals(item.get("descricaoItem").asString())) {
                return item.get("id").asLong();
            }
        }
        throw new IllegalStateException("Item de catalogo nao encontrado no seed: " + descricao);
    }

    private BigDecimal quantidadeAtualDoItem(long estoqueId) throws Exception {
        String resposta = mockMvc.perform(get("/api/estoque/{id}", estoqueId))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return new BigDecimal(objectMapper.readTree(resposta).get("quantidadeAtual").asString());
    }
}
