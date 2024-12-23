package com.alianza.authservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.alianza.authservice.model.Usuario;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Usuario findByEmail(String email);
}
