package com.alianza.create_service.config;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

public class CustomJwtAuthenticationConverter {

	public Collection<GrantedAuthority> extractAuthorities(String rolesClaim) {
	    if (rolesClaim == null || rolesClaim.isEmpty()) {
	        return Collections.emptyList(); // Devuelve una lista vacía si el claim es null o vacío
	    }

	    // Usa Arrays.stream() para convertir el array en un Stream
	    return Arrays.stream(rolesClaim.split(","))
	            .map(String::trim) // Elimina espacios alrededor de los roles
	            .map(SimpleGrantedAuthority::new) // Convierte cada rol en un GrantedAuthority
	            .collect(Collectors.toList());
	}
}