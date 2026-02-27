package com.Rimbest.rimbest.model.dto;

import jakarta.validation.constraints.*;

public class ChambreRequest {
    private String numero;

    private String type;

    @Min(1)
    private Integer capacite;

    @Positive
    private Double prixParNuit;

    private String description;
    private Double prixWeekend;
    private Boolean disponible;

    private Long hotelId;

    private Double superficie;
    private String equipementsChambre;
    private String vueType;
    private Integer nombreLits;
    private String typeLits;
    private Double taxeSejour;
    private Double depotGarantie;
    private Boolean salleBainPrivee;
    private Boolean climatisation;
    private Boolean television;
    private Boolean wifi;
    private Boolean minibar;
    private Boolean coffreFort;
    private String statutNettoyage;
    private java.util.List<String> imagesUrls;

    // Getters and Setters
    public java.util.List<String> getImagesUrls() {
        return imagesUrls;
    }

    public void setImagesUrls(java.util.List<String> imagesUrls) {
        this.imagesUrls = imagesUrls;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Integer getCapacite() {
        return capacite;
    }

    public void setCapacite(Integer capacite) {
        this.capacite = capacite;
    }

    public Double getPrixParNuit() {
        return prixParNuit;
    }

    public void setPrixParNuit(Double prixParNuit) {
        this.prixParNuit = prixParNuit;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Double getPrixWeekend() {
        return prixWeekend;
    }

    public void setPrixWeekend(Double prixWeekend) {
        this.prixWeekend = prixWeekend;
    }

    public Boolean getDisponible() {
        return disponible;
    }

    public void setDisponible(Boolean disponible) {
        this.disponible = disponible;
    }

    public Long getHotelId() {
        return hotelId;
    }

    public void setHotelId(Long hotelId) {
        this.hotelId = hotelId;
    }

    public Double getSuperficie() {
        return superficie;
    }

    public void setSuperficie(Double superficie) {
        this.superficie = superficie;
    }

    public String getEquipementsChambre() {
        return equipementsChambre;
    }

    public void setEquipementsChambre(String equipementsChambre) {
        this.equipementsChambre = equipementsChambre;
    }

    public String getVueType() {
        return vueType;
    }

    public void setVueType(String vueType) {
        this.vueType = vueType;
    }

    public Integer getNombreLits() {
        return nombreLits;
    }

    public void setNombreLits(Integer nombreLits) {
        this.nombreLits = nombreLits;
    }

    public String getTypeLits() {
        return typeLits;
    }

    public void setTypeLits(String typeLits) {
        this.typeLits = typeLits;
    }

    public Double getTaxeSejour() {
        return taxeSejour;
    }

    public void setTaxeSejour(Double taxeSejour) {
        this.taxeSejour = taxeSejour;
    }

    public Double getDepotGarantie() {
        return depotGarantie;
    }

    public void setDepotGarantie(Double depotGarantie) {
        this.depotGarantie = depotGarantie;
    }

    public Boolean getSalleBainPrivee() {
        return salleBainPrivee;
    }

    public void setSalleBainPrivee(Boolean salleBainPrivee) {
        this.salleBainPrivee = salleBainPrivee;
    }

    public Boolean getClimatisation() {
        return climatisation;
    }

    public void setClimatisation(Boolean climatisation) {
        this.climatisation = climatisation;
    }

    public Boolean getTelevision() {
        return television;
    }

    public void setTelevision(Boolean television) {
        this.television = television;
    }

    public Boolean getWifi() {
        return wifi;
    }

    public void setWifi(Boolean wifi) {
        this.wifi = wifi;
    }

    public Boolean getMinibar() {
        return minibar;
    }

    public void setMinibar(Boolean minibar) {
        this.minibar = minibar;
    }

    public Boolean getCoffreFort() {
        return coffreFort;
    }

    public void setCoffreFort(Boolean coffreFort) {
        this.coffreFort = coffreFort;
    }

    public String getStatutNettoyage() {
        return statutNettoyage;
    }

    public void setStatutNettoyage(String statutNettoyage) {
        this.statutNettoyage = statutNettoyage;
    }
}
