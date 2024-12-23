package com.alianza.authservice.controller;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Map;

@RestController
@RequestMapping("/keys")
public class KeyController {

    private final JWKSet jwkSet;

    public KeyController(RSAPublicKey publicKey, RSAPrivateKey privateKey) {
        RSAKey rsaKey = new RSAKey.Builder(publicKey)
                .privateKey(privateKey)
                .keyID("auth-service-key")
                .build();
        this.jwkSet = new JWKSet(rsaKey);
    }

    @GetMapping
    public Map<String, Object> getJwkSet() {
        return jwkSet.toJSONObject();
    }
}