package com.alianza.authservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.io.IOException;

@Configuration
public class RsaKeyConfig {

    @Value("${app.rsa.privateKeyPath}")
    private Resource privateKeyResource;

    @Value("${app.rsa.publicKeyPath}")
    private Resource publicKeyResource;

    @Bean
    public RSAPrivateKey rsaPrivateKey() throws Exception {
        String privateKeyContent = readKeyFromResource(privateKeyResource, "PRIVATE KEY");
        byte[] decoded = Base64.getDecoder().decode(privateKeyContent);

        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(decoded);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return (RSAPrivateKey) kf.generatePrivate(keySpec);
    }

    @Bean
    public RSAPublicKey rsaPublicKey() throws Exception {
        String publicKeyContent = readKeyFromResource(publicKeyResource, "PUBLIC KEY");
        byte[] decoded = Base64.getDecoder().decode(publicKeyContent);

        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(decoded);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return (RSAPublicKey) kf.generatePublic(keySpec);
    }

    private String readKeyFromResource(Resource resource, String keyType) throws IOException {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            boolean isKeyContent = false;
            while ((line = br.readLine()) != null) {
                if (line.contains("BEGIN " + keyType)) {
                    isKeyContent = true;
                } else if (line.contains("END " + keyType)) {
                    break;
                } else if (isKeyContent) {
                    sb.append(line.trim());
                }
            }
            return sb.toString();
        }
    }
}
