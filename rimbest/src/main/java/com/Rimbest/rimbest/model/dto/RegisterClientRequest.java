package com.Rimbest.rimbest.model.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class RegisterClientRequest {
    @NotBlank @Size(max = 50)
    private String nom;

    @NotBlank @Email @Size(max = 100)
    private String email;

    @NotBlank @Size(min = 6, max = 100)
    private String password;

    private String telephone;
    private String adresse;

    // getters et setters

    public String getNom() {
        return nom;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }
    public String getTelephone() {
        return telephone;
    }

    public String getAdresse() {
        return adresse;
    }
}