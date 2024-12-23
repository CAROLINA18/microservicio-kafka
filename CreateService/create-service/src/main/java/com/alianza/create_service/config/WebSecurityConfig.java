package com.alianza.create_service.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;
import java.util.stream.Stream;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationEntryPoint;
import org.springframework.security.oauth2.server.resource.web.access.BearerTokenAccessDeniedHandler;
import static org.springframework.security.config.Customizer.withDefaults;

import java.util.Collection;

import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;


@Configuration
@EnableMethodSecurity  
public class WebSecurityConfig {

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
	    http
	        .csrf(csrf -> csrf.disable())
	        .authorizeHttpRequests(auth -> auth
	            .requestMatchers("/create/**").hasAuthority("CREAR_USUARIO_BD") // Requiere este rol
	            .anyRequest().authenticated() // Cualquier otra solicitud necesita autenticación
	        )
	        .oauth2ResourceServer(oauth2 -> oauth2
	            .jwt(jwt -> jwt
	                .jwkSetUri("http://localhost:8081/auth-service/keys") // Configura el JWK URI
	                .jwtAuthenticationConverter(token -> convertJwtToAuthentication(token)) // Renombrado a 'token'
	            )
	            .authenticationEntryPoint(new BearerTokenAuthenticationEntryPoint()) // Maneja errores de autenticación
	            .accessDeniedHandler(new BearerTokenAccessDeniedHandler()) // Maneja errores de acceso
	        );
	    return http.build();
	}
    
	// Convertidor personalizado de Jwt a JwtAuthenticationToken
	private JwtAuthenticationToken convertJwtToAuthentication(Jwt jwt) {
	    // Extrae el claim "roles" del JWT
	    String rolesClaim = jwt.getClaimAsString("roles");

	    // Si no hay roles, crea un JwtAuthenticationToken sin authorities
	    Collection<GrantedAuthority> authorities = rolesClaim == null || rolesClaim.isEmpty()
	            ? Collections.emptyList()
	            : Stream.of(rolesClaim.split(","))
	                    .map(String::trim) // Limpia espacios
	                    .map(SimpleGrantedAuthority::new) // Convierte a GrantedAuthority
	                    .collect(Collectors.toList());

	    // Retorna un JwtAuthenticationToken con el principal y las authorities
	    return new JwtAuthenticationToken(jwt, authorities, jwt.getSubject());
	}
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
