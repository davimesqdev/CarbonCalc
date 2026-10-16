package br.com.carboncalc.integracao;

/** Armazenamento dos arquivos de relatório gerados. */
public interface StorageService {

    void salvar(String chave, byte[] conteudo, String contentType);

    byte[] ler(String chave);
}
