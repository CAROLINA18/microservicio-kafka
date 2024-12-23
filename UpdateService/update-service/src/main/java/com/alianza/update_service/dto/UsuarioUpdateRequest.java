package com.alianza.update_service.dto;

import lombok.Data;

@Data
public class UsuarioUpdateRequest {
    private Long id;
    private String email;
    private String password;
    private String roles;
    
    public UsuarioUpdateRequest() {}

    public UsuarioUpdateRequest(String email, String password, String roles, Long id) {
    	this.id = id;
        this.email = email;
        this.password = password;
        this.roles = roles;
    }
}
