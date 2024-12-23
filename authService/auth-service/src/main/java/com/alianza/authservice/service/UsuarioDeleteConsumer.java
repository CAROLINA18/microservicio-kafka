package com.alianza.authservice.service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.alianza.authservice.model.Usuario;
import com.alianza.authservice.repository.UsuarioRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class UsuarioDeleteConsumer {

    private final UsuarioRepository usuarioRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public UsuarioDeleteConsumer(UsuarioRepository usuarioRepository, KafkaTemplate<String, String> kafkaTemplate) {
        this.usuarioRepository = usuarioRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = new ObjectMapper();
    }

    @KafkaListener(topics = "USER_DELETET_TOPIC", groupId = "auth-service-delete-group",
            containerFactory = "usuarioReadRequestKafkaListenerContainerFactory")
    public void handleDeleteRequest(String message) {
        System.out.println(message + "------------------------------");
        try {
            // Parsear el mensaje recibido
            Map<String, Object> requestPayload = objectMapper.readValue(message, new TypeReference<>() {});

            // Validar la existencia de las claves y sus valores
            if (!requestPayload.containsKey("id") || requestPayload.get("id") == null) {
                throw new IllegalArgumentException("El campo 'id' es obligatorio y no puede ser nulo.");
            }
            if (!requestPayload.containsKey("requestId") || requestPayload.get("requestId") == null) {
                throw new IllegalArgumentException("El campo 'requestId' es obligatorio y no puede ser nulo.");
            }

            // Convertir 'id' a Long de forma segura
            Long id = ((Number) requestPayload.get("id")).longValue();
            String requestId = (String) requestPayload.get("requestId");

            // Intentar eliminar el usuario
            Optional<Usuario> optionalUsuario = usuarioRepository.findById(id);
            if (optionalUsuario.isPresent()) {
                usuarioRepository.deleteById(id);
                sendResponse(requestId, "Usuario eliminado exitosamente.");
            } else {
                sendResponse(requestId, "Usuario no encontrado.");
            }
        } catch (IllegalArgumentException e) {
            // Manejar errores de validación
            System.err.println("Error en el mensaje recibido: " + e.getMessage());
            sendErrorResponse("Solicitud inválida: " + e.getMessage());
        } catch (Exception e) {
            // Manejar errores inesperados
            System.err.println("Error al procesar la solicitud de eliminación: " + e.getMessage());
            e.printStackTrace();
            sendErrorResponse("Error interno al procesar la solicitud.");
        }
    }

    private void sendResponse(String requestId, String message) {
        try {
            Map<String, String> response = new HashMap<>();
            response.put("requestId", requestId);
            response.put("message", message);

            String responseJson = objectMapper.writeValueAsString(response);
            kafkaTemplate.send("USER_DELETE_RESPONSE_TOPIC", requestId, responseJson);
        } catch (Exception e) {
            System.err.println("Error al enviar la respuesta de eliminación: " + e.getMessage());
        }
    }

    private void sendErrorResponse(String errorMessage) {
        try {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", errorMessage);

            String errorJson = objectMapper.writeValueAsString(errorResponse);
            kafkaTemplate.send("USER_DELETE_RESPONSE_TOPIC", errorJson);
        } catch (Exception e) {
            System.err.println("Error al enviar mensaje de error: " + e.getMessage());
        }
    }
}