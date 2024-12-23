package com.alianza.authservice.service;


import com.alianza.authservice.dto.UsuarioUpdateRequest;
import com.alianza.authservice.model.Usuario;
import com.alianza.authservice.repository.UsuarioRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserUpdateConsumer {

	private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UserUpdateConsumer(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @KafkaListener(topics = "USER_UPDATE_TOPIC", 
            groupId = "auth-service-group", 
            containerFactory = "usuarioUpdateRequestKafkaListenerContainerFactory")
    public void handleUserUpdate(UsuarioUpdateRequest request) {
        try {
            Usuario usuario = usuarioRepository.findById(request.getId())
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

            usuario.setEmail(request.getEmail());
            if (request.getPassword() != null && !request.getPassword().isEmpty()) {
                usuario.setPassword(passwordEncoder.encode(request.getPassword()));
            }
            usuario.setRoles(request.getRoles());

            usuarioRepository.save(usuario);
            System.out.println("Usuario actualizado: " + usuario.getEmail());
        } catch (Exception e) {
            System.err.println("Error al procesar el mensaje de actualización: " + e.getMessage());
        }
    }
}
