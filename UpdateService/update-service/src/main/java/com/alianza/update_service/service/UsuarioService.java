package com.alianza.update_service.service;



import com.alianza.update_service.dto.UsuarioUpdateRequest;
import com.alianza.update_service.model.Usuario;
import com.alianza.update_service.repository.UsuarioRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper; // Inyección de ObjectMapper

    public UsuarioService(UsuarioRepository usuarioRepository, KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
        this.usuarioRepository = usuarioRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper; // Inicialización
    }

    public Usuario actualizarUsuario(UsuarioUpdateRequest request) {
    	System.out.println(request.getId()+"-----------------------AQUI");
        Usuario usuario = usuarioRepository.findById(request.getId())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        usuario.setEmail(request.getEmail());
        usuario.setPassword(request.getPassword());
        usuario.setRoles(request.getRoles());
        Usuario usuarioActualizado = usuarioRepository.save(usuario);

        try {
            // Publicar el evento en Kafka
            String message = objectMapper.writeValueAsString(usuarioActualizado);
            kafkaTemplate.send("USER_UPDATED_TOPIC", message);
        } catch (Exception e) {
            throw new RuntimeException("Error al serializar el usuario actualizado", e);
        }

        return usuarioActualizado;
    }
}
