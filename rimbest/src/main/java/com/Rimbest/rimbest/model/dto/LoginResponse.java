package com.Rimbest.rimbest.model.dto;

import java.util.List;

public class LoginResponse {
    private String token;
    private String type = "Bearer";
    private Long id;
    private String email;
    private String nom;
    private List<RoleInfo> roles;

    public LoginResponse(String token, Long id, String email, String nom, List<String> roles) {
        this.token = token;
        this.id = id;
        this.email = email;
        this.nom = nom;
        this.roles = roles.stream().map(RoleInfo::new).collect(java.util.stream.Collectors.toList());
    }


    // NOUVEAUX GETTERS
    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getNom() {
        return nom;
    }

    public List<RoleInfo> getRoles() {
        return roles;
    }

    public static class RoleInfo {
        private String name;

        public RoleInfo(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
    //getters
    public String getToken() {
        return token;
    }
}