package com.Rimbest.rimbest.model.dto;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import org.hibernate.Hibernate;

public class HotelResponse {
    private Long id;
    private String nom;
    private String adresse;
    private String ville;
    private String description;
    private Integer etoiles;
    private List<String> imagesUrls; // ou List<String>
    private Boolean actif;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long partenaireId;
    private String partenaireNom; // optionnel
    private int nombreChambres; // nombre de chambres dans cet hôtel
    private String telephone;
    private String email;
    private String equipementsHotel;
    private Double prixParNuit; // Prix promo/minimum pour affichage

    // Constructeurs, getters et setters

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

    public String getAdresse() {
        return adresse;
    }

    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }

    public String getVille() {
        return ville;
    }

    public void setVille(String ville) {
        this.ville = ville;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getEtoiles() {
        return etoiles;
    }

    public void setEtoiles(Integer etoiles) {
        this.etoiles = etoiles;
    }

    public List<String> getImagesUrls() {
        return imagesUrls;
    }

    public void setImagesUrls(List<String> imagesUrls) {
        this.imagesUrls = imagesUrls;
    }

    public Boolean getActif() {
        return actif;
    }

    public void setActif(Boolean actif) {
        this.actif = actif;
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

    public Long getPartenaireId() {
        return partenaireId;
    }

    public void setPartenaireId(Long partenaireId) {
        this.partenaireId = partenaireId;
    }

    public String getPartenaireNom() {
        return partenaireNom;
    }

    public void setPartenaireNom(String partenaireNom) {
        this.partenaireNom = partenaireNom;
    }

    public int getNombreChambres() {
        return nombreChambres;
    }

    public void setNombreChambres(int nombreChambres) {
        this.nombreChambres = nombreChambres;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getEquipementsHotel() {
        return equipementsHotel;
    }

    public void setEquipementsHotel(String equipementsHotel) {
        this.equipementsHotel = equipementsHotel;
    }

    public Double getPrixParNuit() {
        return prixParNuit;
    }

    public void setPrixParNuit(Double prixParNuit) {
        this.prixParNuit = prixParNuit;
    }

    public static HotelResponse fromHotel(com.Rimbest.rimbest.model.Hotel hotel) {
        HotelResponse response = new HotelResponse();
        response.setId(hotel.getId());
        response.setNom(hotel.getNom());
        response.setAdresse(hotel.getAdresse());
        response.setVille(hotel.getVille());
        response.setDescription(hotel.getDescription());
        response.setEtoiles(hotel.getEtoiles());
        if (hotel.getImagesUrls() != null && !hotel.getImagesUrls().isEmpty()) {
            response.setImagesUrls(
                    List.of(hotel.getImagesUrls().split(",")));
        }
        response.setActif(hotel.getActif());
        response.setCreatedAt(hotel.getCreatedAt());
        response.setUpdatedAt(hotel.getUpdatedAt());
        if (hotel.getPartenaire() != null) {
            response.setPartenaireId(hotel.getPartenaire().getId());
            if (Hibernate.isInitialized(hotel.getPartenaire())) {
                response.setPartenaireNom(hotel.getPartenaire().getNom());
            } else {
                response.setPartenaireNom(""); // ou charger depuis une autre source
            }
        }
        if (hotel.getChambres() != null) {
            response.setNombreChambres(hotel.getChambres().size());
        }
        response.setTelephone(hotel.getTelephone());
        response.setEmail(hotel.getEmail());
        response.setEquipementsHotel(hotel.getEquipementsHotel());
        response.setPrixParNuit(hotel.getPrixMinimumIndication()); // ← Mappage du prix indicatif
        return response;
    }

}