package br.edu.uninter.gestaodoacoes.service;

import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import br.edu.uninter.gestaodoacoes.exception.ResourceNotFoundException;
import br.edu.uninter.gestaodoacoes.model.DoacaoFinanceira;
import br.edu.uninter.gestaodoacoes.repository.DoacaoFinanceiraRepository;

@Service
@Transactional
public class DoacaoFinanceiraService {

    private final DoacaoFinanceiraRepository doacaoFinanceiraRepository;
    private final ComprovanteStorageService comprovanteStorageService;

    public DoacaoFinanceiraService(DoacaoFinanceiraRepository doacaoFinanceiraRepository,
                                    ComprovanteStorageService comprovanteStorageService) {
        this.doacaoFinanceiraRepository = doacaoFinanceiraRepository;
        this.comprovanteStorageService = comprovanteStorageService;
    }

    public void uploadComprovante(Long doacaoId, MultipartFile arquivo) {
        DoacaoFinanceira doacaoFinanceira = buscarPorDoacaoId(doacaoId);

        String arquivoAnterior = doacaoFinanceira.getComprovanteUrl();
        String novoNomeArquivo = comprovanteStorageService.salvar(arquivo);

        doacaoFinanceira.setComprovanteUrl(novoNomeArquivo);
        doacaoFinanceiraRepository.save(doacaoFinanceira);

        comprovanteStorageService.excluir(arquivoAnterior);
    }

    @Transactional(readOnly = true)
    public Resource baixarComprovante(Long doacaoId) {
        DoacaoFinanceira doacaoFinanceira = buscarPorDoacaoId(doacaoId);
        if (doacaoFinanceira.getComprovanteUrl() == null) {
            throw new ResourceNotFoundException("Nenhum comprovante enviado para a doacao " + doacaoId + ".");
        }
        return comprovanteStorageService.carregar(doacaoFinanceira.getComprovanteUrl());
    }

    private DoacaoFinanceira buscarPorDoacaoId(Long doacaoId) {
        return doacaoFinanceiraRepository.findByDoacaoId(doacaoId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Doacao financeira nao encontrada para a doacao " + doacaoId + "."));
    }
}
