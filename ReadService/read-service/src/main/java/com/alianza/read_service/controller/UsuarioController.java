package com.alianza.read_service.controller;

import com.alianza.read_service.dto.UsuarioResponse;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/users")
public class UsuarioController {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final ConcurrentHashMap<String, CompletableFuture<List<UsuarioResponse>>> allUsersRequests = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, CompletableFuture<UsuarioResponse>> singleUserRequests = new ConcurrentHashMap<>();

    @Value("${kafka.response.timeout:10}")
    private int kafkaResponseTimeout;

    public UsuarioController(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = new ObjectMapper();
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('CONSULTA_USUARIO')")
    public ResponseEntity<?> obtenerUsuarioPorId(@PathVariable Long id) {
        if (id == null || id <= 0) {
            return ResponseEntity.badRequest().body("ID de usuario no válido.");
        }

        String requestId = UUID.randomUUID().toString(); // Generar un ID único para esta solicitud
        try {
            // Crear payload para Kafka
            Map<String, Object> requestPayload = new HashMap<>();
            requestPayload.put("requestId", requestId);
            requestPayload.put("id", id);

            CompletableFuture<UsuarioResponse> future = sendKafkaRequest(
                "USER_READ_REQUEST_TOPIC",
                requestId,
                requestPayload,
                UsuarioResponse.class
            );

            // Esperar la respuesta
            UsuarioResponse response = future.get(kafkaResponseTimeout, TimeUnit.SECONDS);
            return ResponseEntity.ok(response);
        } catch (TimeoutException e) {
            return ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT)
                    .body("La solicitud tomó demasiado tiempo en responder.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al procesar la solicitud: " + e.getMessage());
        } finally {
            singleUserRequests.remove(requestId); // Limpieza del mapa
        }
    }
    

    @GetMapping
    @PreAuthorize("hasAuthority('CONSULTA_USUARIO')")
    public ResponseEntity<?> obtenerTodosLosUsuarios() {
        String requestId = UUID.randomUUID().toString(); // Generar un ID único para la solicitud
        try {
            // Crear un payload JSON válido
            Map<String, Object> requestPayload = new HashMap<>();
            requestPayload.put("requestId", requestId);
            requestPayload.put("action", "ALL_USERS");

            CompletableFuture<List> future = sendKafkaRequest(
                "USER_READ_REQUEST_TOPIC",
                requestId,
                requestPayload,
                List.class
            );

            // Esperar respuesta
            List<UsuarioResponse> responses = future.get(kafkaResponseTimeout, TimeUnit.SECONDS);
            return ResponseEntity.ok(responses);
        } catch (TimeoutException e) {
            return ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT)
                    .body("La solicitud tomó demasiado tiempo en responder.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al procesar la solicitud: " + e.getMessage());
        } finally {
            allUsersRequests.remove(requestId); // Limpieza del mapa
        }
    }

    @KafkaListener(topics = "USER_READ_RESPONSE_TOPIC", groupId = "read-service-group")
    public void handleUserReadResponse(ConsumerRecord<String, String> record) {
        if (record.key() == null || record.value() == null) {
            System.err.println("Registro recibido con clave o valor nulo.");
            return;
        }

        String requestId = record.key();
        String message = record.value();

        try {
            if (singleUserRequests.containsKey(requestId)) {
                // Respuesta para un usuario
                UsuarioResponse response = objectMapper.readValue(message, UsuarioResponse.class);
                CompletableFuture<UsuarioResponse> future = singleUserRequests.remove(requestId);
                if (future != null) {
                    future.complete(response);
                }
            } else if (allUsersRequests.containsKey(requestId)) {
                // Respuesta para todos los usuarios
                List<UsuarioResponse> responses = objectMapper.readValue(
                        message,
                        objectMapper.getTypeFactory().constructCollectionType(List.class, UsuarioResponse.class)
                );
                CompletableFuture<List<UsuarioResponse>> future = allUsersRequests.remove(requestId);
                if (future != null) {
                    future.complete(responses);
                }
            } else {
                System.err.println("RequestId no reconocido: " + requestId);
            }
        } catch (Exception e) {
            System.err.println("Error al procesar la respuesta de Kafka: " + e.getMessage());
        }
    }

    private <T> CompletableFuture<T> sendKafkaRequest(String topic, String requestId, Object payload, Class<T> responseType) throws Exception {
        String jsonMessage = objectMapper.writeValueAsString(payload);
        kafkaTemplate.send(topic, requestId, jsonMessage);

        CompletableFuture<T> future = new CompletableFuture<>();
        if (responseType == UsuarioResponse.class) {
            singleUserRequests.put(requestId, (CompletableFuture<UsuarioResponse>) future);
        } else {
            allUsersRequests.put(requestId, (CompletableFuture<List<UsuarioResponse>>) future);
        }
        return future;
    }
}
