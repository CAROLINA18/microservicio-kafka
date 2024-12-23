package com.alianza.create_service.controller;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import com.alianza.create_service.dto.UsuarioRequest;
import com.alianza.create_service.repository.UsuarioRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/api/create")
public class CreateController {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public CreateController(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = new ObjectMapper(); // Inicializa ObjectMapper
    }

    @PostMapping
    @PreAuthorize("hasAuthority('CREAR_USUARIO_BD')")
    public ResponseEntity<String> createUser(@RequestBody UsuarioRequest request) {
        try {
            // Serializar el objeto UsuarioRequest a JSON
            String jsonMessage = objectMapper.writeValueAsString(request);

            // Publicar el mensaje JSON
            kafkaTemplate.send("USER_CREATION_TOPIC", jsonMessage);

            return ResponseEntity.status(HttpStatus.ACCEPTED)
                    .body("User creation request sent to Kafka");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error serializing the user request");
        }
    }
}

