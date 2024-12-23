package com.alianza.authservice.service;

import org.springframework.ldap.core.LdapTemplate;
import org.springframework.stereotype.Service;
import javax.naming.directory.DirContext;

@Service
public class LdapService {

    private final LdapTemplate ldapTemplate;

    public LdapService(LdapTemplate ldapTemplate) {
        this.ldapTemplate = ldapTemplate;
    }

    public boolean authenticateViaLdap(String email, String password) {
        try {
            // Aquí asumimos que el "email" es un DN o un atributo "uid" .
            String userDn = "uid=" + email + ",dc=alianza,dc=com,dc=co";
            
            DirContext ctx = ldapTemplate.getContextSource().getContext(userDn, password);
            // Si no lanza excepción, es que las credenciales son válidas
            ctx.close();
            return true;
        } catch (Exception e) {
            return false; 
        }
    }
}
