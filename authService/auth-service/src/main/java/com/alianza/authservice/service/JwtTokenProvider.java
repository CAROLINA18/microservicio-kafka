package com.alianza.authservice.service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;
import java.security.interfaces.RSAPrivateKey;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class JwtTokenProvider {

    private final RSAPrivateKey privateKey;

    @Autowired
    public JwtTokenProvider(RSAPrivateKey privateKey) {
        this.privateKey = privateKey;
    }

    public String generateToken(String email, List<GrantedAuthority> authorities) {
        long now = System.currentTimeMillis();
        long expirationInMillis = 1000 * 60 * 60 * 24; // 1 día = 86400000 ms
        Date issuedAt = new Date(now);
        Date expiresAt = new Date(now + expirationInMillis);
        String roles = authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(",")); 

        return Jwts.builder()
                .setSubject(email)
                .claim("roles", roles) // Usar "authorities" como claim
                .setIssuedAt(issuedAt)
                .setExpiration(expiresAt)
                .signWith(privateKey, SignatureAlgorithm.RS256) // RS256: Algoritmo asimétrico
                .compact();
    }
}
