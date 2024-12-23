package com.alianza.delete_service.config;

import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationEntryPoint;
import org.springframework.security.oauth2.server.resource.web.access.BearerTokenAccessDeniedHandler;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource())) // Configuración CORS
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/usuarios/**").hasAuthority("ELIMINA_USUARIO") // Requiere este rol
                .anyRequest().authenticated() // Cualquier otra solicitud necesita autenticación
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt
                    .jwkSetUri("http://localhost:8081/auth-service/keys") // Configura el JWK URI
                    .jwtAuthenticationConverter(token -> convertJwtToAuthentication(token)) // Convertir JWT
                )
                .authenticationEntryPoint(new BearerTokenAuthenticationEntryPoint()) // Maneja errores de autenticación
                .accessDeniedHandler(new BearerTokenAccessDeniedHandler()) // Maneja errores de acceso
            );
        return http.build();
    }

    // Configuración global de CORS
    private UrlBasedCorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.addAllowedOrigin("http://localhost:4200"); // Permitir solicitudes desde Angular
        configuration.addAllowedMethod("*"); // Permitir todos los métodos (GET, POST, PUT, DELETE, etc.)
        configuration.addAllowedHeader("*"); // Permitir todos los encabezados
        configuration.setAllowCredentials(true); // Permitir credenciales como cookies o headers de autorización

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration); // Aplicar configuración a todas las rutas
        return source;
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
}
