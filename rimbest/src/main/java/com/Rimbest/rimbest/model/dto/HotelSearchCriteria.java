package com.Rimbest.rimbest.model.dto;

public class HotelSearchCriteria {
    private String ville;
    private Integer etoilesMin;
    private Boolean actif;
    private String search; // recherche textuelle dans nom ou ville
    // getters/setters
    public String getVille() {
        return ville;
    }
    public void setVille(String ville) {
        this.ville = ville;
    }
    public Integer getEtoilesMin() {
        return etoilesMin;
    }
    public void setEtoilesMin(Integer etoilesMin) {
        this.etoilesMin = etoilesMin;
    }
    public Boolean getActif() {
        return actif;
    }
    public void setActif(Boolean actif) {
        this.actif = actif;
    }
    public String getSearch() {
        return search;
    }
    public void setSearch(String search) {
        this.search = search;
    }
}