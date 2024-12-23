package com.alianza.authservice.service;


import com.alianza.authservice.dto.UsuarioUpdateRequest;
import com.alianza.authservice.model.Usuario;
import com.alianza.authservice.repository.UsuarioRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StringDeserializer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class UsuarioReadConsumer {

    private final UsuarioRepository usuarioRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public UsuarioReadConsumer(UsuarioRepository usuarioRepository, KafkaTemplate<String, String> kafkaTemplate) {
        this.usuarioRepository = usuarioRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = new ObjectMapper();
    }

    @KafkaListener(topics = "USER_READ_REQUEST_TOPIC", groupId = "auth-service-group-new",
                   containerFactory = "usuarioReadRequestKafkaListenerContainerFactory")
    public void handleUserReadRequest(String message) {
        try {
            // Deserializar el mensaje recibido
            UsuarioUpdateRequest request = objectMapper.readValue(message, UsuarioUpdateRequest.class);

            if (request.getAction() != null && request.getAction().equals("ALL_USERS")) {
                handleAllUsersRequest(request.getRequestId());
            }else if (request.getId() != null) {
                handleSingleUserRequest(request.getId(), request.getRequestId().toString());
            } else {
                System.err.println("Acción no reconocida o mensaje inválido: " + message);
            }
        } catch (Exception e) {
            System.err.println("Error al procesar el mensaje de Kafka: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleSingleUserRequest(Long id, String requestId) {
        try {
            Optional<Usuario> optionalUsuario = usuarioRepository.findById(id);
            if (optionalUsuario.isPresent()) {
                Usuario usuario = optionalUsuario.get();
                UsuarioUpdateRequest response = convertirAUsuarioResponse(usuario);

                // Serializar la respuesta como JSON
                String responseJson = objectMapper.writeValueAsString(response);

                // Publicar la respuesta en Kafka
                kafkaTemplate.send("USER_READ_RESPONSE_TOPIC", requestId, responseJson);
                System.out.println("Usuario encontrado y enviado a Kafka: " + usuario.getEmail());
            } else {
                System.err.println("Usuario no encontrado con ID: " + id);
            }
        } catch (Exception e) {
            System.err.println("Error al procesar la solicitud de un usuario: " + e.getMessage());
        }
    }

    private void handleAllUsersRequest(String requestId) {
        try {
            List<Usuario> usuarios = usuarioRepository.findAll();
            List<UsuarioUpdateRequest> responseList = usuarios.stream()
                    .map(this::convertirAUsuarioResponse)
                    .collect(Collectors.toList());

            // Serializar la lista de usuarios como JSON
            String responseJson = objectMapper.writeValueAsString(responseList);

            // Publicar la respuesta en Kafka
            kafkaTemplate.send("USER_READ_RESPONSE_TOPIC", requestId, responseJson);
            System.out.println("Todos los usuarios enviados a Kafka.");
        } catch (Exception e) {
            System.err.println("Error al procesar la solicitud de todos los usuarios: " + e.getMessage());
        }
    }

    private UsuarioUpdateRequest convertirAUsuarioResponse(Usuario usuario) {
        UsuarioUpdateRequest response = new UsuarioUpdateRequest();
        response.setId(usuario.getId());
        response.setEmail(usuario.getEmail());
        response.setRoles(usuario.getRoles());
        return response;
    }


}

