package br.edu.uninter.gestaodoacoes.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import br.edu.uninter.gestaodoacoes.dto.DadosBancariosRequestDTO;
import br.edu.uninter.gestaodoacoes.dto.DadosBancariosResponseDTO;
import br.edu.uninter.gestaodoacoes.model.DadosBancarios;
import br.edu.uninter.gestaodoacoes.repository.DadosBancariosRepository;

@ExtendWith(MockitoExtension.class)
class DadosBancariosServiceTest {

    @Mock
    private DadosBancariosRepository dadosBancariosRepository;

    private DadosBancariosService dadosBancariosService;

    @BeforeEach
    void setUp() {
        dadosBancariosService = new DadosBancariosService(dadosBancariosRepository);
    }

    @Test
    void deveAtualizarDadosBancariosExistentes() {
        DadosBancarios existente = DadosBancarios.builder()
                .id(DadosBancarios.ID_UNICO).banco("A definir").agencia("A definir")
                .conta("A definir").chavePix("A definir").titular("A definir").build();

        DadosBancariosRequestDTO request = new DadosBancariosRequestDTO(
                "Banco Exemplo", "0001", "12345-6", "contato@associacao.org", "Associacao Espirita");

        when(dadosBancariosRepository.findById(DadosBancarios.ID_UNICO)).thenReturn(Optional.of(existente));
        when(dadosBancariosRepository.save(any(DadosBancarios.class))).thenAnswer(inv -> inv.getArgument(0));

        DadosBancariosResponseDTO response = dadosBancariosService.atualizar(request);

        assertThat(response.banco()).isEqualTo("Banco Exemplo");
        assertThat(response.chavePix()).isEqualTo("contato@associacao.org");
    }

    @Test
    void deveBuscarDadosBancariosConfigurados() {
        DadosBancarios existente = DadosBancarios.builder()
                .id(DadosBancarios.ID_UNICO).banco("Banco X").agencia("1").conta("2")
                .chavePix("chave").titular("Titular").build();

        when(dadosBancariosRepository.findById(DadosBancarios.ID_UNICO)).thenReturn(Optional.of(existente));

        DadosBancariosResponseDTO response = dadosBancariosService.buscar();

        assertThat(response.banco()).isEqualTo("Banco X");
    }
}
