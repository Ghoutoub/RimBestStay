package com.Rimbest.rimbest.model.dto;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

public class UserResponse {
    private Long id;
    private String nom;
    private String email;
    private Boolean actif;
    private List<RoleInfo> roles; // Changed from List<String>
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String type; // "CLIENT", "PARTENAIRE", "ADMIN"

    // Champs spécifiques Client
    private String telephone;
    private String adresse;

    // Champs spécifiques Partenaire
    private String nomEntreprise;
    private String siret;
    private String adresseEntreprise;
    private String telephonePro;
    private String siteWeb;
    private String description;
    private Boolean verified;
    private Double commissionRate;

    // Champs spécifiques Administrateur
    private String departement;

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getAdresse() {
        return adresse;
    }

    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }

    public String getNomEntreprise() {
        return nomEntreprise;
    }

    public void setNomEntreprise(String nomEntreprise) {
        this.nomEntreprise = nomEntreprise;
    }

    public String getSiret() {
        return siret;
    }

    public void setSiret(String siret) {
        this.siret = siret;
    }

    public String getAdresseEntreprise() {
        return adresseEntreprise;
    }

    public void setAdresseEntreprise(String adresseEntreprise) {
        this.adresseEntreprise = adresseEntreprise;
    }

    public String getTelephonePro() {
        return telephonePro;
    }

    public void setTelephonePro(String telephonePro) {
        this.telephonePro = telephonePro;
    }

    public String getSiteWeb() {
        return siteWeb;
    }

    public void setSiteWeb(String siteWeb) {
        this.siteWeb = siteWeb;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getVerified() {
        return verified;
    }

    public void setVerified(Boolean verified) {
        this.verified = verified;
    }

    public Double getCommissionRate() {
        return commissionRate;
    }

    public void setCommissionRate(Double commissionRate) {
        this.commissionRate = commissionRate;
    }

    public String getDepartement() {
        return departement;
    }

    public void setDepartement(String departement) {
        this.departement = departement;
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

    // Constructeurs, getters et setters
    public UserResponse() {
    }

    // Getters et setters pour tous les champs...
    // (à générer automatiquement par votre IDE)
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Boolean getActif() {
        return actif;
    }

    public void setActif(Boolean actif) {
        this.actif = actif;
    }

    public List<RoleInfo> getRoles() {
        return roles;
    }

    public void setRoles(List<RoleInfo> roles) {
        this.roles = roles;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;

    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTelephone() {
        return telephone;
    }

    // Méthode utilitaire pour construire une UserResponse à partir d'un User
    public static UserResponse fromUser(com.Rimbest.rimbest.model.User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setNom(user.getNom());
        response.setEmail(user.getEmail());
        response.setActif(user.getActif());

        if (user.getCreatedAt() != null) {
            response.setCreatedAt(Instant.ofEpochMilli(user.getCreatedAt().getTime())
                    .atZone(ZoneId.systemDefault()).toLocalDateTime());
        }
        if (user.getUpdatedAt() != null) {
            response.setUpdatedAt(Instant.ofEpochMilli(user.getUpdatedAt().getTime())
                    .atZone(ZoneId.systemDefault()).toLocalDateTime());
        }

        if (user.getRoles() != null) {
            response.setRoles(user.getRoles().stream()
                    .map(role -> new RoleInfo(role.getName().name()))
                    .collect(Collectors.toList()));
        }

        // Déterminer le type et remplir les champs spécifiques
        if (user instanceof com.Rimbest.rimbest.model.Client) {
            response.setType("CLIENT");
            com.Rimbest.rimbest.model.Client client = (com.Rimbest.rimbest.model.Client) user;
            response.setTelephone(client.getTelephone());
            response.setAdresse(client.getAdresse());
        } else if (user instanceof com.Rimbest.rimbest.model.Partenaire) {
            response.setType("PARTENAIRE");
            com.Rimbest.rimbest.model.Partenaire partenaire = (com.Rimbest.rimbest.model.Partenaire) user;
            response.setNomEntreprise(partenaire.getNomEntreprise());
            response.setSiret(partenaire.getSiret());
            response.setAdresseEntreprise(partenaire.getAdresseEntreprise());
            response.setTelephonePro(partenaire.getTelephonePro());
            response.setSiteWeb(partenaire.getSiteWeb());
            response.setDescription(partenaire.getDescription());
            response.setVerified(partenaire.isVerified());
            response.setCommissionRate(partenaire.getCommissionRate());
        } else if (user instanceof com.Rimbest.rimbest.model.Administrator) {
            response.setType("ADMIN");
            com.Rimbest.rimbest.model.Administrator admin = (com.Rimbest.rimbest.model.Administrator) user;
            response.setDepartement(admin.getDepartement());
        } else {
            response.setType("UNKNOWN");
        }

        return response;
    }

}