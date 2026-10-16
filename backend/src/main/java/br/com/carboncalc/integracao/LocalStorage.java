package br.com.carboncalc.integracao;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

/** Guarda os relatórios em uma pasta local; substitui o S3 no perfil "local", sem Docker. */
@Service
@Profile("local")
public class LocalStorage implements StorageService {

    private final Path raiz;

    public LocalStorage(@Value("${carboncalc.storage.local-dir}") String diretorio) {
        this.raiz = Path.of(diretorio).toAbsolutePath().normalize();
    }

    @Override
    public void salvar(String chave, byte[] conteudo, String contentType) {
        try {
            Path destino = resolver(chave);
            Files.createDirectories(destino.getParent());
            Files.write(destino, conteudo);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public byte[] ler(String chave) {
        try {
            return Files.readAllBytes(resolver(chave));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private Path resolver(String chave) {
        Path destino = raiz.resolve(chave).normalize();
        if (!destino.startsWith(raiz)) {
            throw new IllegalArgumentException("Chave fora do diretório de armazenamento: " + chave);
        }
        return destino;
    }
}
