package com.alianza.update_service.controller;



import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.alianza.update_service.dto.UsuarioUpdateRequest;
import com.alianza.update_service.model.Usuario;
import com.alianza.update_service.service.UsuarioService;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/api/update")
public class UsuarioController {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public UsuarioController(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = new ObjectMapper();
    }

    @PutMapping
    @PreAuthorize("hasAuthority('ACTUALIZA_USUARIO')")
    public ResponseEntity<String> updateUser(@RequestBody UsuarioUpdateRequest request) {
        try {
            // Serializar el objeto UsuarioUpdateRequest a JSON
            String jsonMessage = objectMapper.writeValueAsString(request);

            // Publicar el mensaje JSON
            kafkaTemplate.send("USER_UPDATE_TOPIC", jsonMessage);

            return ResponseEntity.status(HttpStatus.ACCEPTED)
                    .body("User update request sent to Kafka");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error serializing the user update request");
        }
    }
}
