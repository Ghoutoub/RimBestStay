package com.Rimbest.rimbest.model.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class UserUpdateDTO {
    
    @NotBlank(message = "Le nom est obligatoire")
    @Size(max = 50, message = "Le nom ne peut pas dépasser 50 caractères")
    private String nom;
    
    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "Format d'email invalide")
    @Size(max = 100, message = "L'email ne peut pas dépasser 100 caractères")
    private String email;
    
    private Boolean actif = true;
    
    // Champs spécifiques au client
    private String telephone;
    private String adresse;
    
    // Champs spécifiques au partenaire
    private String nomEntreprise;
    private String siret;
    private String adresseEntreprise;
    private String telephonePro;
    private String siteWeb;
    private String description;
    private boolean verified = false;
    private Double commissionRate;
    
    // Champs spécifiques à l'administrateur
    private String departement;
    
    // Getters et Setters
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public Boolean getActif() { return actif; }
    public void setActif(Boolean actif) { this.actif = actif; }
    
    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }
    
    public String getAdresse() { return adresse; }
    public void setAdresse(String adresse) { this.adresse = adresse; }
    
    public String getNomEntreprise() { return nomEntreprise; }
    public void setNomEntreprise(String nomEntreprise) { this.nomEntreprise = nomEntreprise; }
    
    public String getSiret() { return siret; }
    public void setSiret(String siret) { this.siret = siret; }
    
    public String getAdresseEntreprise() { return adresseEntreprise; }
    public void setAdresseEntreprise(String adresseEntreprise) { this.adresseEntreprise = adresseEntreprise; }
    
    public String getTelephonePro() { return telephonePro; }
    public void setTelephonePro(String telephonePro) { this.telephonePro = telephonePro; }
    
    public String getSiteWeb() { return siteWeb; }
    public void setSiteWeb(String siteWeb) { this.siteWeb = siteWeb; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public boolean isVerified() { return verified; }
    public void setVerified(boolean verified) { this.verified = verified; }
    
    public Double getCommissionRate() { return commissionRate; }
    public void setCommissionRate(Double commissionRate) { this.commissionRate = commissionRate; }
    
    public String getDepartement() { return departement; }
    public void setDepartement(String departement) { this.departement = departement; }
}