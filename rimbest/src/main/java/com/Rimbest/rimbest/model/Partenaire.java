package com.Rimbest.rimbest.model;

import jakarta.persistence.*;

@Entity
@Table(name = "partenaires")
@PrimaryKeyJoinColumn(name = "user_id")
// @DiscriminatorValue("PARTENAIRE")
public class Partenaire extends User {
    
    @Column(name = "nom_entreprise", nullable = false, length = 100)
    private String nomEntreprise;
    
    @Column(name = "siret", unique = true, length = 14)
    private String siret;
    
    @Column(name = "adresse_entreprise", length = 200)
    private String adresseEntreprise;
    
    @Column(name = "telephone_pro", length = 20)
    private String telephonePro;
    
    @Column(name = "site_web")
    private String siteWeb;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "verified", nullable = false)
    private boolean verified = false;
    
    @Column(name = "commission_rate")
    private Double commissionRate = 15.0; // Taux de commission par défaut
    
    // Constructeurs
    public Partenaire() {}
    
    public Partenaire(String nom, String email, String motDePasse, String nomEntreprise) {
        super(nom, email, motDePasse);
        this.nomEntreprise = nomEntreprise;
    }
    
    public Partenaire(String nom, String email, String motDePasse, String nomEntreprise, String siret) {
        super(nom, email, motDePasse);
        this.nomEntreprise = nomEntreprise;
        this.setSiret(siret); // Using setter to handle empty strings
    }
    
    // Getters et Setters
    public String getNomEntreprise() { return nomEntreprise; }
    public void setNomEntreprise(String nomEntreprise) { this.nomEntreprise = nomEntreprise; }
    
    public String getSiret() { return siret; }
    public void setSiret(String siret) { 
        this.siret = (siret != null && !siret.trim().isEmpty()) ? siret.trim() : null; 
    }
    
    public String getAdresseEntreprise() { return adresseEntreprise; }
    public void setAdresseEntreprise(String adresseEntreprise) { 
        this.adresseEntreprise = (adresseEntreprise != null && !adresseEntreprise.trim().isEmpty()) ? adresseEntreprise.trim() : null; 
    }
    
    public String getTelephonePro() { return telephonePro; }
    public void setTelephonePro(String telephonePro) { 
        this.telephonePro = (telephonePro != null && !telephonePro.trim().isEmpty()) ? telephonePro.trim() : null; 
    }
    
    public String getSiteWeb() { return siteWeb; }
    public void setSiteWeb(String siteWeb) { 
        this.siteWeb = (siteWeb != null && !siteWeb.trim().isEmpty()) ? siteWeb.trim() : null; 
    }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public boolean isVerified() { return verified; }
    public void setVerified(boolean verified) { this.verified = verified; }
    
    public Double getCommissionRate() { return commissionRate; }
    public void setCommissionRate(Double commissionRate) { this.commissionRate = commissionRate; }
}