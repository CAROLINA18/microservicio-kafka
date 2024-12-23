package com.alianza.authservice.controller;

import com.alianza.authservice.dto.LoginRequest;
import com.alianza.authservice.dto.TokenResponse;
import com.alianza.authservice.exception.InvalidCredentialsException;
import com.alianza.authservice.service.AuthService;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    // Inyección vía constructor
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> authenticate(@RequestBody LoginRequest loginRequest) {
        try {
            // Llamamos al servicio para la autenticación
            String token = authService.authenticate(loginRequest);
            return ResponseEntity.ok(new TokenResponse(token));
        } catch (InvalidCredentialsException e) {
            // Si no hay credenciales válidas, retornamos 401
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }
    
}
