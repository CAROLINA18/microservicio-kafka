package com.alianza.read_service.repository;


import org.springframework.data.jpa.repository.JpaRepository;

import com.alianza.read_service.model.Usuario;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
}