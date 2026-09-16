package br.edu.uninter.gestaodoacoes.service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

@Service
@Transactional
public class AgendamentoService {

    private final AgendamentoRepository agendamentoRepository;
    private final DoacaoRepository doacaoRepository;
    private final HorarioAtendimentoRepository horarioAtendimentoRepository;

    public AgendamentoService(AgendamentoRepository agendamentoRepository,
                               DoacaoRepository doacaoRepository,
                               HorarioAtendimentoRepository horarioAtendimentoRepository) {
        this.agendamentoRepository = agendamentoRepository;
        this.doacaoRepository = doacaoRepository;
        this.horarioAtendimentoRepository = horarioAtendimentoRepository;
    }

    public AgendamentoResponseDTO criar(AgendamentoRequestDTO request) {
        Doacao doacao = doacaoRepository.findById(request.doacaoId())
                .orElseThrow(() -> new ResourceNotFoundException("Doacao nao encontrada: id " + request.doacaoId()));

        if (doacao.getTipo() == TipoDoacao.FINANCEIRA) {
            throw new RegraNegocioException("Doacoes do tipo FINANCEIRA nao utilizam agendamento de entrega presencial.");
        }

        if (agendamentoRepository.findByDoacaoId(doacao.getId()).isPresent()) {
            throw new RegraNegocioException("Esta doacao ja possui um agendamento.");
        }

        HorarioAtendimento horario = horarioAtendimentoRepository.findById(request.horarioAtendimentoId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Horario de atendimento nao encontrado: id " + request.horarioAtendimentoId()));

        if (request.dataEntrega().isBefore(LocalDate.now())) {
            throw new RegraNegocioException("Data de entrega nao pode ser no passado.");
        }

        if (request.dataEntrega().getDayOfWeek() != horario.getDiaSemana()) {
            throw new RegraNegocioException("A data de entrega (" + request.dataEntrega().getDayOfWeek()
                    + ") nao corresponde ao dia da semana do horario selecionado (" + horario.getDiaSemana() + ").");
        }

        long vagasOcupadas = agendamentoRepository.countByHorarioAtendimentoIdAndDataEntrega(
                horario.getId(), request.dataEntrega());
        if (vagasOcupadas >= horario.getCapacidadeMaxima()) {
            throw new RegraNegocioException("Nao ha vagas disponiveis para este horario nesta data.");
        }

        Agendamento agendamento = Agendamento.builder()
                .doacao(doacao)
                .horarioAtendimento(horario)
                .dataEntrega(request.dataEntrega())
                .build();

        return toResponseDTO(agendamentoRepository.save(agendamento));
    }

    @Transactional(readOnly = true)
    public AgendamentoResponseDTO buscarPorId(Long id) {
        Agendamento agendamento = agendamentoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Agendamento nao encontrado: id " + id));
        return toResponseDTO(agendamento);
    }

    @Transactional(readOnly = true)
    public Optional<AgendamentoResponseDTO> buscarPorDoacaoId(Long doacaoId) {
        return agendamentoRepository.findByDoacaoId(doacaoId).map(this::toResponseDTO);
    }

    @Transactional(readOnly = true)
    public List<AgendamentoResponseDTO> listar() {
        return agendamentoRepository.findAll().stream()
                .map(this::toResponseDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<DisponibilidadeHorarioDTO> consultarDisponibilidade(LocalDate data) {
        DayOfWeek diaSemana = data.getDayOfWeek();
        return horarioAtendimentoRepository.findByDiaSemana(diaSemana).stream()
                .map(horario -> {
                    long vagasOcupadas = agendamentoRepository.countByHorarioAtendimentoIdAndDataEntrega(
                            horario.getId(), data);
                    int vagasDisponiveis = (int) Math.max(0, horario.getCapacidadeMaxima() - vagasOcupadas);
                    return new DisponibilidadeHorarioDTO(
                            horario.getId(),
                            data,
                            diaSemana,
                            horario.getHoraInicio(),
                            horario.getHoraFim(),
                            horario.getCapacidadeMaxima(),
                            (int) vagasOcupadas,
                            vagasDisponiveis
                    );
                })
                .toList();
    }

    private AgendamentoResponseDTO toResponseDTO(Agendamento agendamento) {
        return new AgendamentoResponseDTO(
                agendamento.getId(),
                agendamento.getDoacao().getId(),
                agendamento.getHorarioAtendimento().getId(),
                agendamento.getDataEntrega(),
                agendamento.getHorarioAtendimento().getHoraInicio(),
                agendamento.getHorarioAtendimento().getHoraFim()
        );
    }
}
