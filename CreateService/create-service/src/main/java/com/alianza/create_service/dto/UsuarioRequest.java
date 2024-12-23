package com.alianza.create_service.dto;

import java.io.Serializable;

import lombok.Data;

@Data
public class UsuarioRequest {
    private String email;
    private String password;
    private String roles;
    
    public UsuarioRequest() {}

    public UsuarioRequest(String email, String password, String roles) {
        this.email = email;
        this.password = password;
        this.roles = roles;
    }
}
