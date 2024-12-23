package com.alianza.create_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.alianza.create_service.model.Usuario;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

}
