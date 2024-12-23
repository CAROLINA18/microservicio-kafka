package com.alianza.delete_service.controller;


import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/usuarios")
public class UsuarioDeleteController {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public UsuarioDeleteController(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = new ObjectMapper();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ELIMINA_USUARIO')")
    public ResponseEntity<?> eliminarUsuario(@PathVariable Long id) {
        String requestId = UUID.randomUUID().toString();
        try {
            // Crear payload para Kafka
            Map<String, Object> requestPayload = new HashMap<>();
            requestPayload.put("requestId", requestId);
            requestPayload.put("id", id);
            // Publicar solicitud a Kafka
            kafkaTemplate.send("USER_DELETET_TOPIC", requestId, objectMapper.writeValueAsString(requestPayload));

            return ResponseEntity.ok("Solicitud de eliminación enviada con requestId: " + requestId );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al enviar la solicitud de eliminación: " + e.getMessage());
        }
    }
}

