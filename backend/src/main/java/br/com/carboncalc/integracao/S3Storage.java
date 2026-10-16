package br.com.carboncalc.integracao;

import java.net.URI;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.NoSuchBucketException;

/** Armazena os arquivos de relatório em um bucket S3 (MinIO no ambiente com Docker). */
@Service
@Profile("!local")
public class S3Storage implements StorageService {

    private final S3Client s3;
    private final String bucket;
    private volatile boolean bucketVerificado;

    public S3Storage(
            @Value("${carboncalc.storage.endpoint}") String endpoint,
            @Value("${carboncalc.storage.region}") String region,
            @Value("${carboncalc.storage.bucket}") String bucket,
            @Value("${carboncalc.storage.access-key}") String accessKey,
            @Value("${carboncalc.storage.secret-key}") String secretKey) {
        this.bucket = bucket;
        this.s3 = S3Client.builder()
                .endpointOverride(URI.create(endpoint))
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(accessKey, secretKey)))
                .forcePathStyle(true)
                .build();
    }

    @Override
    public void salvar(String chave, byte[] conteudo, String contentType) {
        garantirBucket();
        s3.putObject(b -> b.bucket(bucket).key(chave).contentType(contentType),
                RequestBody.fromBytes(conteudo));
    }

    @Override
    public byte[] ler(String chave) {
        return s3.getObjectAsBytes(b -> b.bucket(bucket).key(chave)).asByteArray();
    }

    // O bucket é criado sob demanda para que a aplicação suba mesmo com o storage fora do ar.
    private void garantirBucket() {
        if (bucketVerificado) {
            return;
        }
        try {
            s3.headBucket(b -> b.bucket(bucket));
        } catch (NoSuchBucketException e) {
            s3.createBucket(b -> b.bucket(bucket));
        }
        bucketVerificado = true;
    }
}
