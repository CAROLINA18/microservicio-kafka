package com.alianza.update_service.repository;


import org.springframework.data.jpa.repository.JpaRepository;

import com.alianza.update_service.model.Usuario;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
}
