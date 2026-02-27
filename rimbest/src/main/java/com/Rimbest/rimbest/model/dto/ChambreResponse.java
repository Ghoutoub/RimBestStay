package com.Rimbest.rimbest.model.dto;

import com.Rimbest.rimbest.model.Chambre;

public class ChambreResponse {
    private Long id;
    private String numero;
    private String type; // Renommé pour correspondre au frontend
    private Double prixParNuit; // Renommé pour correspondre au frontend
    private Double prixWeekend;
    private Integer capacite;
    private String description;
    private Boolean disponible;
    private Long hotelId;
    private String hotelNom;
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
    private String imagesChambre;

    public static ChambreResponse fromChambre(Chambre chambre) {
        ChambreResponse response = new ChambreResponse();
        response.setId(chambre.getId());
        response.setNumero(chambre.getNumero());
        response.setType(chambre.getTypeChambre());
        response.setPrixParNuit(chambre.getPrixNuit());
        response.setPrixWeekend(chambre.getPrixWeekend());
        response.setCapacite(chambre.getCapacite());
        response.setDescription(chambre.getDescription());
        response.setDisponible(chambre.getDisponible());
        if (chambre.getHotel() != null) {
            response.setHotelId(chambre.getHotel().getId());
            response.setHotelNom(chambre.getHotel().getNom());
        }
        response.setSuperficie(chambre.getSuperficie());
        response.setEquipementsChambre(chambre.getEquipementsChambre());
        response.setVueType(chambre.getVueType());
        response.setNombreLits(chambre.getNombreLits());
        response.setTypeLits(chambre.getTypeLits());
        response.setTaxeSejour(chambre.getTaxeSejour());
        response.setDepotGarantie(chambre.getDepotGarantie());
        response.setSalleBainPrivee(chambre.getSalleBainPrivee());
        response.setClimatisation(chambre.getClimatisation());
        response.setTelevision(chambre.getTelevision());
        response.setWifi(chambre.getWifi());
        response.setMinibar(chambre.getMinibar());
        response.setCoffreFort(chambre.getCoffreFort());
        response.setStatutNettoyage(chambre.getStatutNettoyage());
        response.setImagesChambre(chambre.getImagesChambre());
        return response;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Double getPrixParNuit() {
        return prixParNuit;
    }

    public void setPrixParNuit(Double prixParNuit) {
        this.prixParNuit = prixParNuit;
    }

    public Double getPrixWeekend() {
        return prixWeekend;
    }

    public void setPrixWeekend(Double prixWeekend) {
        this.prixWeekend = prixWeekend;
    }

    public Integer getCapacite() {
        return capacite;
    }

    public void setCapacite(Integer capacite) {
        this.capacite = capacite;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public String getHotelNom() {
        return hotelNom;
    }

    public void setHotelNom(String hotelNom) {
        this.hotelNom = hotelNom;
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

    public String getImagesChambre() {
        return imagesChambre;
    }

    public void setImagesChambre(String imagesChambre) {
        this.imagesChambre = imagesChambre;
    }
}
