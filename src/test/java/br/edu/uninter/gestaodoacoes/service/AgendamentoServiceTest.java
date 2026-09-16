package br.edu.uninter.gestaodoacoes.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import br.edu.uninter.gestaodoacoes.dto.AgendamentoRequestDTO;
import br.edu.uninter.gestaodoacoes.dto.AgendamentoResponseDTO;
import br.edu.uninter.gestaodoacoes.dto.DisponibilidadeHorarioDTO;
import br.edu.uninter.gestaodoacoes.exception.RegraNegocioException;
import br.edu.uninter.gestaodoacoes.exception.ResourceNotFoundException;
import br.edu.uninter.gestaodoacoes.model.Agendamento;
import br.edu.uninter.gestaodoacoes.model.Doacao;
import br.edu.uninter.gestaodoacoes.model.HorarioAtendimento;
import br.edu.uninter.gestaodoacoes.model.TipoDoacao;
import br.edu.uninter.gestaodoacoes.repository.AgendamentoRepository;
import br.edu.uninter.gestaodoacoes.repository.DoacaoRepository;
import br.edu.uninter.gestaodoacoes.repository.HorarioAtendimentoRepository;

@ExtendWith(MockitoExtension.class)
class AgendamentoServiceTest {

    @Mock
    private AgendamentoRepository agendamentoRepository;
    @Mock
    private DoacaoRepository doacaoRepository;
    @Mock
    private HorarioAtendimentoRepository horarioAtendimentoRepository;

    private AgendamentoService agendamentoService;

    // proxima segunda-feira a partir de hoje (sempre no futuro), para os testes nao dependerem do dia em que rodam
    private final LocalDate proximaSegunda = LocalDate.now()
            .with(TemporalAdjusters.next(DayOfWeek.MONDAY));

    @BeforeEach
    void setUp() {
        agendamentoService = new AgendamentoService(agendamentoRepository, doacaoRepository, horarioAtendimentoRepository);
    }

    private Doacao criarDoacao() {
        return Doacao.builder().id(1L).build();
    }

    private HorarioAtendimento criarHorarioSegunda(int capacidade) {
        return HorarioAtendimento.builder()
                .id(1L)
                .diaSemana(DayOfWeek.MONDAY)
                .horaInicio(LocalTime.of(9, 0))
                .horaFim(LocalTime.of(12, 0))
                .capacidadeMaxima(capacidade)
                .build();
    }

    @Test
    void deveCriarAgendamentoQuandoHaVagas() {
        Doacao doacao = criarDoacao();
        HorarioAtendimento horario = criarHorarioSegunda(2);
        AgendamentoRequestDTO request = new AgendamentoRequestDTO(1L, 1L, proximaSegunda);

        when(doacaoRepository.findById(1L)).thenReturn(Optional.of(doacao));
        when(agendamentoRepository.findByDoacaoId(1L)).thenReturn(Optional.empty());
        when(horarioAtendimentoRepository.findById(1L)).thenReturn(Optional.of(horario));
        when(agendamentoRepository.countByHorarioAtendimentoIdAndDataEntrega(1L, proximaSegunda)).thenReturn(1L);
        when(agendamentoRepository.save(any(Agendamento.class))).thenAnswer(inv -> {
            Agendamento a = inv.getArgument(0);
            a.setId(50L);
            return a;
        });

        AgendamentoResponseDTO response = agendamentoService.criar(request);

        assertThat(response.id()).isEqualTo(50L);
        assertThat(response.dataEntrega()).isEqualTo(proximaSegunda);
    }

    @Test
    void deveRecusarQuandoCapacidadeEsgotada() {
        Doacao doacao = criarDoacao();
        HorarioAtendimento horario = criarHorarioSegunda(2);
        AgendamentoRequestDTO request = new AgendamentoRequestDTO(1L, 1L, proximaSegunda);

        when(doacaoRepository.findById(1L)).thenReturn(Optional.of(doacao));
        when(agendamentoRepository.findByDoacaoId(1L)).thenReturn(Optional.empty());
        when(horarioAtendimentoRepository.findById(1L)).thenReturn(Optional.of(horario));
        when(agendamentoRepository.countByHorarioAtendimentoIdAndDataEntrega(1L, proximaSegunda)).thenReturn(2L);

        assertThatThrownBy(() -> agendamentoService.criar(request))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("Nao ha vagas");
    }

    @Test
    void deveRecusarQuandoDiaDaSemanaNaoBate() {
        Doacao doacao = criarDoacao();
        HorarioAtendimento horario = criarHorarioSegunda(5);
        LocalDate terca = proximaSegunda.plusDays(1);
        AgendamentoRequestDTO request = new AgendamentoRequestDTO(1L, 1L, terca);

        when(doacaoRepository.findById(1L)).thenReturn(Optional.of(doacao));
        when(agendamentoRepository.findByDoacaoId(1L)).thenReturn(Optional.empty());
        when(horarioAtendimentoRepository.findById(1L)).thenReturn(Optional.of(horario));

        assertThatThrownBy(() -> agendamentoService.criar(request))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("nao corresponde ao dia da semana");
    }

    @Test
    void deveRecusarQuandoDoacaoJaTemAgendamento() {
        Doacao doacao = criarDoacao();
        AgendamentoRequestDTO request = new AgendamentoRequestDTO(1L, 1L, proximaSegunda);

        when(doacaoRepository.findById(1L)).thenReturn(Optional.of(doacao));
        when(agendamentoRepository.findByDoacaoId(1L)).thenReturn(Optional.of(Agendamento.builder().id(99L).build()));

        assertThatThrownBy(() -> agendamentoService.criar(request))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("ja possui um agendamento");
    }

    @Test
    void deveRecusarDataNoPassado() {
        Doacao doacao = criarDoacao();
        HorarioAtendimento horario = criarHorarioSegunda(5);
        LocalDate segundaPassada = proximaSegunda.minusWeeks(1);
        AgendamentoRequestDTO request = new AgendamentoRequestDTO(1L, 1L, segundaPassada);

        when(doacaoRepository.findById(1L)).thenReturn(Optional.of(doacao));
        when(agendamentoRepository.findByDoacaoId(1L)).thenReturn(Optional.empty());
        when(horarioAtendimentoRepository.findById(1L)).thenReturn(Optional.of(horario));

        assertThatThrownBy(() -> agendamentoService.criar(request))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("nao pode ser no passado");
    }

    @Test
    void deveRecusarAgendamentoParaDoacaoFinanceira() {
        Doacao doacaoFinanceira = Doacao.builder().id(2L).tipo(TipoDoacao.FINANCEIRA).build();
        AgendamentoRequestDTO request = new AgendamentoRequestDTO(2L, 1L, proximaSegunda);

        when(doacaoRepository.findById(2L)).thenReturn(Optional.of(doacaoFinanceira));

        assertThatThrownBy(() -> agendamentoService.criar(request))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("FINANCEIRA nao utilizam agendamento");
    }

    @Test
    void deveLancarNotFoundQuandoDoacaoNaoExiste() {
        AgendamentoRequestDTO request = new AgendamentoRequestDTO(404L, 1L, proximaSegunda);
        when(doacaoRepository.findById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> agendamentoService.criar(request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void deveCalcularDisponibilidadeCorretamente() {
        HorarioAtendimento horario = criarHorarioSegunda(3);
        when(horarioAtendimentoRepository.findByDiaSemana(DayOfWeek.MONDAY)).thenReturn(List.of(horario));
        when(agendamentoRepository.countByHorarioAtendimentoIdAndDataEntrega(1L, proximaSegunda)).thenReturn(2L);

        List<DisponibilidadeHorarioDTO> disponibilidade = agendamentoService.consultarDisponibilidade(proximaSegunda);

        assertThat(disponibilidade).hasSize(1);
        assertThat(disponibilidade.get(0).vagasOcupadas()).isEqualTo(2);
        assertThat(disponibilidade.get(0).vagasDisponiveis()).isEqualTo(1);
    }
}
