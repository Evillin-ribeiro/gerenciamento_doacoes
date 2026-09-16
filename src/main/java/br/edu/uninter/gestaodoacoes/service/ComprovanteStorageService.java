package br.edu.uninter.gestaodoacoes.service;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import br.edu.uninter.gestaodoacoes.exception.RegraNegocioException;
import br.edu.uninter.gestaodoacoes.exception.ResourceNotFoundException;

/**
 * Responsavel apenas pelo I/O do comprovante em disco local (pasta configuravel,
 * fora do controle de versao). Regras de negocio da doacao financeira ficam em DoacaoFinanceiraService.
 */
@Service
public class ComprovanteStorageService {

    private static final Set<String> TIPOS_PERMITIDOS = Set.of(
            "image/jpeg", "image/png", "application/pdf");

    private final Path diretorioBase;

    public ComprovanteStorageService(@Value("${uploads.comprovantes-dir}") String diretorioConfigurado) {
        this.diretorioBase = Path.of(diretorioConfigurado).toAbsolutePath().normalize();
    }

    public String salvar(MultipartFile arquivo) {
        if (arquivo == null || arquivo.isEmpty()) {
            throw new RegraNegocioException("Arquivo de comprovante nao pode estar vazio.");
        }
        if (!TIPOS_PERMITIDOS.contains(arquivo.getContentType())) {
            throw new RegraNegocioException("Tipo de arquivo nao permitido. Envie JPEG, PNG ou PDF.");
        }

        try {
            Files.createDirectories(diretorioBase);

            String extensao = extrairExtensao(arquivo.getOriginalFilename());
            String nomeArquivo = UUID.randomUUID() + extensao;
            Path destino = diretorioBase.resolve(nomeArquivo).normalize();

            arquivo.transferTo(destino);
            return nomeArquivo;
        } catch (IOException e) {
            throw new UncheckedIOException("Falha ao salvar o arquivo de comprovante.", e);
        }
    }

    public Resource carregar(String nomeArquivo) {
        try {
            Path arquivo = resolverDentroDoDiretorioBase(nomeArquivo);
            Resource recurso = new UrlResource(arquivo.toUri());
            if (!recurso.exists() || !recurso.isReadable()) {
                throw new ResourceNotFoundException("Arquivo de comprovante nao encontrado: " + nomeArquivo);
            }
            return recurso;
        } catch (MalformedURLException e) {
            throw new ResourceNotFoundException("Arquivo de comprovante nao encontrado: " + nomeArquivo);
        }
    }

    public void excluir(String nomeArquivo) {
        if (!StringUtils.hasText(nomeArquivo)) {
            return;
        }
        try {
            Files.deleteIfExists(resolverDentroDoDiretorioBase(nomeArquivo));
        } catch (IOException e) {
            throw new UncheckedIOException("Falha ao excluir o arquivo de comprovante anterior.", e);
        }
    }

    private Path resolverDentroDoDiretorioBase(String nomeArquivo) {
        Path arquivo = diretorioBase.resolve(nomeArquivo).normalize();
        if (!arquivo.startsWith(diretorioBase)) {
            throw new RegraNegocioException("Nome de arquivo invalido.");
        }
        return arquivo;
    }

    private String extrairExtensao(String nomeOriginal) {
        if (!StringUtils.hasText(nomeOriginal) || !nomeOriginal.contains(".")) {
            return "";
        }
        return nomeOriginal.substring(nomeOriginal.lastIndexOf('.'));
    }
}
