package com.alianza.authservice.service;
import java.util.stream.*;
import com.alianza.authservice.model.Usuario;
import com.alianza.authservice.repository.UsuarioRepository;



import com.alianza.authservice.dto.LoginRequest;
import com.alianza.authservice.exception.InvalidCredentialsException;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final LdapService ldapService;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthService(UsuarioRepository usuarioRepository,
                       PasswordEncoder passwordEncoder,
                       LdapService ldapService,
                       JwtTokenProvider jwtTokenProvider) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.ldapService = ldapService;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    public String authenticate(LoginRequest loginRequest) {
        // 1. Buscar usuario en H2
        Usuario user = usuarioRepository.findByEmail(loginRequest.getEmail());
        
        List<String> roles = Arrays.stream(user.getRoles().split(","))
        	    .map(String::trim) // Elimina espacios en blanco alrededor de los roles
        	    .collect(Collectors.toList());
        
        List<GrantedAuthority> authorities = convertRolesToGrantedAuthorities(roles);

        // Verificar el contenido de la lista de roles
        System.out.println("Roles extraídos: " + authorities);
        
        
        // 2. Verificar password en DB
        if (user != null && passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            return jwtTokenProvider.generateToken(user.getEmail(), authorities);
        }

        // 3. Si no coincide en DB, intentar LDAP
        boolean validLdap = ldapService.authenticateViaLdap(loginRequest.getEmail(), loginRequest.getPassword());
        if (validLdap) {
            // 3.1 Generar JWT con roles de LDAP (o fijos)
        	List<GrantedAuthority> authoritiesLdap = List.of(new SimpleGrantedAuthority("ROLE_CONSULTA_USUARIO_LDAP"));
            return jwtTokenProvider.generateToken(loginRequest.getEmail(),
            		authoritiesLdap);
        }
        
        // 4. Si falla todo, lanzar excepción
        throw new InvalidCredentialsException("Credenciales inválidas");
    }
    
    
    public List<GrantedAuthority> convertRolesToGrantedAuthorities(List<String> roles) {
        return roles.stream()
                .map(role -> new SimpleGrantedAuthority(role))
                .collect(Collectors.toList());
    }
}
