package com.alianza.authservice.dto;


import lombok.Data;

@Data
public class UsuarioUpdateRequest {
	private String requestId;
	private String action;
    private Long id;
    private String email;
    private String password;
    private String roles;
    
    public UsuarioUpdateRequest() {}

    public UsuarioUpdateRequest(String email, String password, String roles, Long id , String action, String requestId) {
    	this.action = action;
    	this.id = id;
    	this.requestId = requestId;
        this.email = email;
        this.password = password;
        this.roles = roles;
    }
}
