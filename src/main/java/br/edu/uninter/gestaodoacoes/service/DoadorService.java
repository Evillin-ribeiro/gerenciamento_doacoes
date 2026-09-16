package br.edu.uninter.gestaodoacoes.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.edu.uninter.gestaodoacoes.dto.DoadorRequestDTO;
import br.edu.uninter.gestaodoacoes.dto.DoadorResponseDTO;
import br.edu.uninter.gestaodoacoes.exception.ResourceNotFoundException;
import br.edu.uninter.gestaodoacoes.model.Doador;
import br.edu.uninter.gestaodoacoes.repository.DoadorRepository;

@Service
@Transactional
public class DoadorService {

    private final DoadorRepository doadorRepository;

    public DoadorService(DoadorRepository doadorRepository) {
        this.doadorRepository = doadorRepository;
    }

    public DoadorResponseDTO criar(DoadorRequestDTO request) {
        Doador doador = Doador.builder()
                .nome(request.nome())
                .email(request.email())
                .telefone(request.telefone())
                .endereco(request.endereco())
                .build();

        return toResponseDTO(doadorRepository.save(doador));
    }

    @Transactional(readOnly = true)
    public DoadorResponseDTO buscarPorId(Long id) {
        return toResponseDTO(buscarEntidadePorId(id));
    }

    @Transactional(readOnly = true)
    public List<DoadorResponseDTO> listar() {
        return doadorRepository.findAll().stream()
                .map(this::toResponseDTO)
                .toList();
    }

    public DoadorResponseDTO atualizar(Long id, DoadorRequestDTO request) {
        Doador doador = buscarEntidadePorId(id);
        doador.setNome(request.nome());
        doador.setEmail(request.email());
        doador.setTelefone(request.telefone());
        doador.setEndereco(request.endereco());

        return toResponseDTO(doadorRepository.save(doador));
    }

    public void remover(Long id) {
        Doador doador = buscarEntidadePorId(id);
        doadorRepository.delete(doador);
    }

    private Doador buscarEntidadePorId(Long id) {
        return doadorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Doador nao encontrado: id " + id));
    }

    private DoadorResponseDTO toResponseDTO(Doador doador) {
        return new DoadorResponseDTO(
                doador.getId(),
                doador.getNome(),
                doador.getEmail(),
                doador.getTelefone(),
                doador.getEndereco()
        );
    }
}
