package br.edu.uninter.gestaodoacoes.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.edu.uninter.gestaodoacoes.dto.DadosBancariosRequestDTO;
import br.edu.uninter.gestaodoacoes.dto.DadosBancariosResponseDTO;
import br.edu.uninter.gestaodoacoes.exception.ResourceNotFoundException;
import br.edu.uninter.gestaodoacoes.model.DadosBancarios;
import br.edu.uninter.gestaodoacoes.repository.DadosBancariosRepository;

@Service
@Transactional
public class DadosBancariosService {

    private final DadosBancariosRepository dadosBancariosRepository;

    public DadosBancariosService(DadosBancariosRepository dadosBancariosRepository) {
        this.dadosBancariosRepository = dadosBancariosRepository;
    }

    @Transactional(readOnly = true)
    public DadosBancariosResponseDTO buscar() {
        return toResponseDTO(buscarEntidade());
    }

    public DadosBancariosResponseDTO atualizar(DadosBancariosRequestDTO request) {
        DadosBancarios dadosBancarios = buscarEntidade();
        dadosBancarios.setBanco(request.banco());
        dadosBancarios.setAgencia(request.agencia());
        dadosBancarios.setConta(request.conta());
        dadosBancarios.setChavePix(request.chavePix());
        dadosBancarios.setTitular(request.titular());

        return toResponseDTO(dadosBancariosRepository.save(dadosBancarios));
    }

    private DadosBancarios buscarEntidade() {
        return dadosBancariosRepository.findById(DadosBancarios.ID_UNICO)
                .orElseThrow(() -> new ResourceNotFoundException("Dados bancarios nao configurados."));
    }

    private DadosBancariosResponseDTO toResponseDTO(DadosBancarios dadosBancarios) {
        return new DadosBancariosResponseDTO(
                dadosBancarios.getBanco(),
                dadosBancarios.getAgencia(),
                dadosBancarios.getConta(),
                dadosBancarios.getChavePix(),
                dadosBancarios.getTitular()
        );
    }
}
