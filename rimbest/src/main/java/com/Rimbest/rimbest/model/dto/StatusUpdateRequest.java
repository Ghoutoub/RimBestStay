package com.Rimbest.rimbest.model.dto;

import lombok.Data;

@Data
public class StatusUpdateRequest {
    private boolean activate; // Utilisé par AdminUserService (enabled) et UserRestController (status)
    private String statut; // Utilisé par ReservationService (CONFIRMEE, REFUSEE, ANNULEE)

    // Getters and Setters explicites pour assurer la compatibilité
    public boolean isActivate() {
        return activate;
    }

    public void setActivate(boolean activate) {
        this.activate = activate;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }
}