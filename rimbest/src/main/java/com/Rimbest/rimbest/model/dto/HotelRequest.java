package com.Rimbest.rimbest.model.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;

public class HotelRequest {

    @Size(max = 100)
    private String nom;

    // @NotBlank retiré pour rendre l'adresse facultative
    @Size(max = 100)
    private String adresse;

    @Size(max = 50)
    private String ville;

    @Size(max = 50)
    private String pays;

    private String description;

    @Min(1)
    private Integer etoiles;

    private List<String> imagesUrls;
    private Boolean actif = true;
    private Long partenaireId;
    private String telephone;
    private String email;
    private String equipementsHotel;

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

    public String getPays() {
        return pays;
    }

    public void setPays(String pays) {
        this.pays = pays;
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

    public Long getPartenaireId() {
        return partenaireId;
    }

    public void setPartenaireId(Long partenaireId) {
        this.partenaireId = partenaireId;
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

    // Getters et setters inchangés (voir code original)
    // ...
}