package br.edu.uninter.gestaodoacoes.integration;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

import br.edu.uninter.gestaodoacoes.dto.AgendamentoRequestDTO;
import br.edu.uninter.gestaodoacoes.dto.DoacaoRequestDTO;
import br.edu.uninter.gestaodoacoes.dto.DoadorRequestDTO;
import br.edu.uninter.gestaodoacoes.dto.HorarioAtendimentoRequestDTO;
import br.edu.uninter.gestaodoacoes.dto.ItemDoacaoRequestDTO;
import br.edu.uninter.gestaodoacoes.model.TipoDoacao;
import tools.jackson.databind.JsonNode;

class AgendamentoFluxoIntegrationTest extends IntegrationTestBase {

    @Test
    void deveConsultarDisponibilidadeAgendarEOcuparVaga() throws Exception {
        LocalDate hoje = LocalDate.now();
        long horarioId = criarHorarioAtendimento(hoje.getDayOfWeek(), 1);
        long doacaoId = criarDoacaoRoupaPendente();

        mockMvc.perform(get("/api/agendamentos/disponibilidade").param("data", hoje.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].vagasDisponiveis").value(1))
                .andExpect(jsonPath("$[0].vagasOcupadas").value(0));

        mockMvc.perform(post("/api/agendamentos")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new AgendamentoRequestDTO(doacaoId, horarioId, hoje))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.dataEntrega").value(hoje.toString()));

        mockMvc.perform(get("/api/agendamentos/disponibilidade").param("data", hoje.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].vagasDisponiveis").value(0))
                .andExpect(jsonPath("$[0].vagasOcupadas").value(1));
    }

    @Test
    void naoDevePermitirAgendarSemVagaDisponivel() throws Exception {
        LocalDate hoje = LocalDate.now();
        long horarioId = criarHorarioAtendimento(hoje.getDayOfWeek(), 1);

        long primeiraDoacaoId = criarDoacaoRoupaPendente();
        mockMvc.perform(post("/api/agendamentos")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new AgendamentoRequestDTO(primeiraDoacaoId, horarioId, hoje))))
                .andExpect(status().isCreated());

        long segundaDoacaoId = criarDoacaoRoupaPendente();
        mockMvc.perform(post("/api/agendamentos")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new AgendamentoRequestDTO(segundaDoacaoId, horarioId, hoje))))
                .andExpect(status().isUnprocessableContent());
    }

    @Test
    void naoDevePermitirDoisAgendamentosParaAMesmaDoacao() throws Exception {
        LocalDate hoje = LocalDate.now();
        long horarioId = criarHorarioAtendimento(hoje.getDayOfWeek(), 5);
        long doacaoId = criarDoacaoRoupaPendente();

        AgendamentoRequestDTO request = new AgendamentoRequestDTO(doacaoId, horarioId, hoje);
        mockMvc.perform(post("/api/agendamentos")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/agendamentos")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableContent());
    }

    @Test
    void naoDevePermitirDataComDiaDaSemanaDiferenteDoHorario() throws Exception {
        LocalDate hoje = LocalDate.now();
        long horarioId = criarHorarioAtendimento(hoje.getDayOfWeek(), 5);
        long doacaoId = criarDoacaoRoupaPendente();

        AgendamentoRequestDTO request = new AgendamentoRequestDTO(doacaoId, horarioId, hoje.plusDays(1));
        mockMvc.perform(post("/api/agendamentos")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableContent());
    }

    private long criarHorarioAtendimento(java.time.DayOfWeek diaSemana, int capacidadeMaxima) throws Exception {
        HorarioAtendimentoRequestDTO request = new HorarioAtendimentoRequestDTO(
                diaSemana, java.time.LocalTime.of(8, 0), java.time.LocalTime.of(10, 0), capacidadeMaxima);

        String resposta = mockMvc.perform(post("/api/horarios-atendimento")
                        .header("Authorization", bearer(tokenAdmin()))
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(resposta).get("id").asLong();
    }

    private long criarDoacaoRoupaPendente() throws Exception {
        String email = "doador-agenda-" + System.nanoTime() + "@teste.local";
        DoadorRequestDTO doador = new DoadorRequestDTO("Doador Agenda", email, "11977776666", null);
        String respostaDoador = mockMvc.perform(post("/api/doadores")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(doador)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        long doadorId = objectMapper.readTree(respostaDoador).get("id").asLong();

        DoacaoRequestDTO request = new DoacaoRequestDTO(doadorId, TipoDoacao.ROUPA,
                List.of(new ItemDoacaoRequestDTO(buscarIdDoItemNoCatalogo("Roupa Infantil"), new BigDecimal("1"))), null);
        String respostaDoacao = mockMvc.perform(post("/api/doacoes")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(respostaDoacao).get("id").asLong();
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
}
