package br.edu.uninter.gestaodoacoes.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

import br.edu.uninter.gestaodoacoes.dto.ConfirmarDoacaoRequestDTO;
import br.edu.uninter.gestaodoacoes.dto.DistribuicaoItemRequestDTO;
import br.edu.uninter.gestaodoacoes.dto.DistribuicaoRequestDTO;
import br.edu.uninter.gestaodoacoes.dto.DoacaoRequestDTO;
import br.edu.uninter.gestaodoacoes.dto.DoadorRequestDTO;
import br.edu.uninter.gestaodoacoes.dto.EstoqueRequestDTO;
import br.edu.uninter.gestaodoacoes.dto.ItemDoacaoRequestDTO;
import br.edu.uninter.gestaodoacoes.model.TipoDoacao;

class DistribuicaoFluxoIntegrationTest extends IntegrationTestBase {

    @Test
    void deveRecusarDistribuicaoQuandoEstoqueInsuficiente() throws Exception {
        long estoqueId = criarItemEstoque();
        String token = bearer(tokenAdmin());

        DistribuicaoRequestDTO request = new DistribuicaoRequestDTO(usuarioIdAdmin(), "Familia Teste", null,
                List.of(new DistribuicaoItemRequestDTO(estoqueId, new BigDecimal("1"))));

        mockMvc.perform(post("/api/distribuicoes")
                        .header("Authorization", token)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableContent());
    }

    @Test
    void deveDistribuirItemComEstoqueEAbaterSaldo() throws Exception {
        long estoqueId = criarItemEstoque();
        String token = bearer(tokenAdmin());
        darEntradaViaDoacaoConfirmada(estoqueId, new BigDecimal("10"));

        DistribuicaoRequestDTO request = new DistribuicaoRequestDTO(usuarioIdAdmin(), "Familia Teste", "entrega semanal",
                List.of(new DistribuicaoItemRequestDTO(estoqueId, new BigDecimal("4"))));

        mockMvc.perform(post("/api/distribuicoes")
                        .header("Authorization", token)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.beneficiario").value("Familia Teste"))
                .andExpect(jsonPath("$.itens[0].quantidade").value(4));

        mockMvc.perform(get("/api/estoque/{id}", estoqueId).header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantidadeAtual").value(6));

        DistribuicaoRequestDTO excedente = new DistribuicaoRequestDTO(usuarioIdAdmin(), "Outra Familia", null,
                List.of(new DistribuicaoItemRequestDTO(estoqueId, new BigDecimal("100"))));
        mockMvc.perform(post("/api/distribuicoes")
                        .header("Authorization", token)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(excedente)))
                .andExpect(status().isUnprocessableContent());
    }

    @Test
    void relatorioDeMovimentacoesDeveRefletirEntradaESaida() throws Exception {
        long estoqueId = criarItemEstoque();
        String token = bearer(tokenAdmin());
        darEntradaViaDoacaoConfirmada(estoqueId, new BigDecimal("8"));

        DistribuicaoRequestDTO request = new DistribuicaoRequestDTO(usuarioIdAdmin(), "Familia Relatorio", null,
                List.of(new DistribuicaoItemRequestDTO(estoqueId, new BigDecimal("3"))));
        mockMvc.perform(post("/api/distribuicoes")
                        .header("Authorization", token)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        LocalDate hoje = LocalDate.now();
        String resposta = mockMvc.perform(get("/api/relatorios/movimentacoes")
                        .header("Authorization", token)
                        .param("periodoInicio", hoje.toString())
                        .param("periodoFim", hoje.toString()))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        var relatorio = objectMapper.readTree(resposta);
        var itemNoRelatorio = itemDoRelatorio(relatorio, estoqueId);
        assertThat(itemNoRelatorio).isNotNull();
        assertThat(new BigDecimal(itemNoRelatorio.get("totalEntradas").asString())).isEqualByComparingTo("8");
        assertThat(new BigDecimal(itemNoRelatorio.get("totalSaidas").asString())).isEqualByComparingTo("3");
        assertThat(new BigDecimal(itemNoRelatorio.get("saldoAtual").asString())).isEqualByComparingTo("5");
    }

    private tools.jackson.databind.JsonNode itemDoRelatorio(tools.jackson.databind.JsonNode relatorio, long estoqueId) {
        for (var item : relatorio.get("itensEstoque")) {
            if (item.get("estoqueId").asLong() == estoqueId) {
                return item;
            }
        }
        return null;
    }

    private void darEntradaViaDoacaoConfirmada(long estoqueId, BigDecimal quantidade) throws Exception {
        String email = "doador-dist-" + System.nanoTime() + "@teste.local";
        DoadorRequestDTO doador = new DoadorRequestDTO("Doador Distribuicao", email, "11966665555", null);
        String respostaDoador = mockMvc.perform(post("/api/doadores")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(doador)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        long doadorId = objectMapper.readTree(respostaDoador).get("id").asLong();

        DoacaoRequestDTO doacaoRequest = new DoacaoRequestDTO(doadorId, TipoDoacao.ITEM_DIVERSO,
                List.of(new ItemDoacaoRequestDTO(estoqueId, quantidade)), null);
        String respostaDoacao = mockMvc.perform(post("/api/doacoes")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(doacaoRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        long doacaoId = objectMapper.readTree(respostaDoacao).get("id").asLong();

        mockMvc.perform(post("/api/doacoes/{id}/confirmar", doacaoId)
                        .header("Authorization", bearer(tokenAdmin()))
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ConfirmarDoacaoRequestDTO(usuarioIdAdmin()))))
                .andExpect(status().isOk());
    }

    private long criarItemEstoque() throws Exception {
        String descricao = "Item Teste Distribuicao " + System.nanoTime();
        EstoqueRequestDTO request = new EstoqueRequestDTO(descricao, "ITEM_DIVERSO", "unidade");

        String resposta = mockMvc.perform(post("/api/estoque")
                        .header("Authorization", bearer(tokenAdmin()))
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(resposta).get("id").asLong();
    }
}
