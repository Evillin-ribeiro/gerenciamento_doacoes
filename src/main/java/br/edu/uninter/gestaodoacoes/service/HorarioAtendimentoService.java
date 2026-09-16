package br.edu.uninter.gestaodoacoes.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.edu.uninter.gestaodoacoes.dto.HorarioAtendimentoRequestDTO;
import br.edu.uninter.gestaodoacoes.dto.HorarioAtendimentoResponseDTO;
import br.edu.uninter.gestaodoacoes.exception.RegraNegocioException;
import br.edu.uninter.gestaodoacoes.exception.ResourceNotFoundException;
import br.edu.uninter.gestaodoacoes.model.HorarioAtendimento;
import br.edu.uninter.gestaodoacoes.repository.HorarioAtendimentoRepository;

@Service
@Transactional
public class HorarioAtendimentoService {

    private final HorarioAtendimentoRepository horarioAtendimentoRepository;

    public HorarioAtendimentoService(HorarioAtendimentoRepository horarioAtendimentoRepository) {
        this.horarioAtendimentoRepository = horarioAtendimentoRepository;
    }

    public HorarioAtendimentoResponseDTO criar(HorarioAtendimentoRequestDTO request) {
        validarIntervalo(request);

        HorarioAtendimento horario = HorarioAtendimento.builder()
                .diaSemana(request.diaSemana())
                .horaInicio(request.horaInicio())
                .horaFim(request.horaFim())
                .capacidadeMaxima(request.capacidadeMaxima())
                .build();

        return toResponseDTO(horarioAtendimentoRepository.save(horario));
    }

    @Transactional(readOnly = true)
    public HorarioAtendimentoResponseDTO buscarPorId(Long id) {
        return toResponseDTO(buscarEntidadePorId(id));
    }

    @Transactional(readOnly = true)
    public List<HorarioAtendimentoResponseDTO> listar() {
        return horarioAtendimentoRepository.findAll().stream()
                .map(this::toResponseDTO)
                .toList();
    }

    public HorarioAtendimentoResponseDTO atualizar(Long id, HorarioAtendimentoRequestDTO request) {
        validarIntervalo(request);

        HorarioAtendimento horario = buscarEntidadePorId(id);
        horario.setDiaSemana(request.diaSemana());
        horario.setHoraInicio(request.horaInicio());
        horario.setHoraFim(request.horaFim());
        horario.setCapacidadeMaxima(request.capacidadeMaxima());

        return toResponseDTO(horarioAtendimentoRepository.save(horario));
    }

    public void remover(Long id) {
        HorarioAtendimento horario = buscarEntidadePorId(id);
        horarioAtendimentoRepository.delete(horario);
    }

    HorarioAtendimento buscarEntidadePorId(Long id) {
        return horarioAtendimentoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Horario de atendimento nao encontrado: id " + id));
    }

    private void validarIntervalo(HorarioAtendimentoRequestDTO request) {
        if (!request.horaInicio().isBefore(request.horaFim())) {
            throw new RegraNegocioException("horaInicio deve ser anterior a horaFim.");
        }
    }

    private HorarioAtendimentoResponseDTO toResponseDTO(HorarioAtendimento horario) {
        return new HorarioAtendimentoResponseDTO(
                horario.getId(),
                horario.getDiaSemana(),
                horario.getHoraInicio(),
                horario.getHoraFim(),
                horario.getCapacidadeMaxima()
        );
    }
}
