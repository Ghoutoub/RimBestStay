package com.Rimbest.rimbest.model.dto;

import com.Rimbest.rimbest.model.ERole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class UserRegistrationDTO {
    
    @NotBlank(message = "Le nom est obligatoire")
    @Size(max = 50, message = "Le nom ne peut pas dépasser 50 caractères")
    private String nom;
    
    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "Format d'email invalide")
    @Size(max = 100, message = "L'email ne peut pas dépasser 100 caractères")
    private String email;
    
    @NotBlank(message = "Le mot de passe est obligatoire")
    @Size(min = 6, message = "Le mot de passe doit contenir au moins 6 caractères")
    private String motDePasse;
    
    @NotBlank(message = "La confirmation du mot de passe est obligatoire")
    private String confirmPassword;
    
    @NotNull(message = "Le rôle est obligatoire")
    private ERole role;
    
    // Champs optionnels selon le rôle
    private String telephone;
    private String adresse;
    private String nomEntreprise;
    private String siret;
    private String departement;
    
    // Getters et Setters
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getMotDePasse() { return motDePasse; }
    public void setMotDePasse(String motDePasse) { this.motDePasse = motDePasse; }
    
    public String getConfirmPassword() { return confirmPassword; }
    public void setConfirmPassword(String confirmPassword) { this.confirmPassword = confirmPassword; }
    
    public ERole getRole() { return role; }
    public void setRole(ERole role) { this.role = role; }
    
    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }
    
    public String getAdresse() { return adresse; }
    public void setAdresse(String adresse) { this.adresse = adresse; }
    
    public String getNomEntreprise() { return nomEntreprise; }
    public void setNomEntreprise(String nomEntreprise) { this.nomEntreprise = nomEntreprise; }
    
    public String getSiret() { return siret; }
    public void setSiret(String siret) { this.siret = siret; }
    
    public String getDepartement() { return departement; }
    public void setDepartement(String departement) { this.departement = departement; }
}