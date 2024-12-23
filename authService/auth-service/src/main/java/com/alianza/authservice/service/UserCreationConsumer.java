package com.alianza.authservice.service;

import com.alianza.authservice.dto.UsuarioRequest;
import com.alianza.authservice.model.Usuario;
import com.alianza.authservice.repository.UsuarioRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserCreationConsumer {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final ObjectMapper objectMapper;

    public UserCreationConsumer(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder, ObjectMapper objectMapper) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.objectMapper = objectMapper; // Inyecta el ObjectMapper
    }
    
   

    @KafkaListener(topics = "USER_CREATION_TOPIC", 
            groupId = "auth-service-group", 
            containerFactory = "usuarioRequestKafkaListenerContainerFactory")
    public void handleUserCreation(UsuarioRequest request) {
        try {
            // Crear y guardar el usuario en la base de datos
            Usuario user = new Usuario();
            user.setEmail(request.getEmail());
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            user.setRoles(request.getRoles());

            usuarioRepository.save(user);

            System.out.println("Usuario guardado: " + user.getEmail());
        } catch (Exception e) {
            System.err.println("Error al procesar el mensaje: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

