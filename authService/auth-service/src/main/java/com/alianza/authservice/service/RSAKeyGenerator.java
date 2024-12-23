package com.alianza.authservice.service;

import java.io.FileOutputStream;
import java.io.IOException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;

public class RSAKeyGenerator {

    public static void main(String[] args) {
        try {
            // Generar el par de claves RSA
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            keyGen.initialize(2048); // Tamaño de la clave
            KeyPair pair = keyGen.generateKeyPair();
            PrivateKey privateKey = pair.getPrivate();
            PublicKey publicKey = pair.getPublic();

            // Convertir las claves a formato PEM
            String privateKeyPEM = "-----BEGIN PRIVATE KEY-----\n" +
                    Base64.getMimeEncoder(64, "\n".getBytes()).encodeToString(privateKey.getEncoded()) +
                    "\n-----END PRIVATE KEY-----\n";

            String publicKeyPEM = "-----BEGIN PUBLIC KEY-----\n" +
                    Base64.getMimeEncoder(64, "\n".getBytes()).encodeToString(publicKey.getEncoded()) +
                    "\n-----END PUBLIC KEY-----\n";

            // Guardar las claves en archivos
            try (FileOutputStream fos = new FileOutputStream("private.pem")) {
                fos.write(privateKeyPEM.getBytes());
            }

            try (FileOutputStream fos = new FileOutputStream("public.pem")) {
                fos.write(publicKeyPEM.getBytes());
            }

            System.out.println("Claves RSA generadas exitosamente.");

        } catch (NoSuchAlgorithmException | IOException e) {
            e.printStackTrace();
        }
    }
}
