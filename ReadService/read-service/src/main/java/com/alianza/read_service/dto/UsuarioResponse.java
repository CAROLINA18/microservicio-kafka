package com.alianza.read_service.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;

@Data
public class UsuarioResponse {
	@JsonIgnore
	private String requestId;
	
	@JsonIgnore
	private String action;
	
    private Long id;
    
    private String email;
    
    @JsonIgnore
    private String password;
    
    
    private String roles;
}
