package com.Rimbest.rimbest.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HotelCardDTO {
    private Long id;
    private String nom;
    private String ville;
    private String pays;
    private String quartier;
    private String adresse;
    private Integer etoiles;
    private String imageUrl;
    private List<String> imagesUrls;
    private Double noteMoyenne;
    private Integer nombreAvis;
    private Double prixMinimum;
    private Double prixMaximum;
    private String prixAffiche;
    private List<String> equipementsHotel;
    private String descriptionCourte;
    private Double distance; // en km
    private Boolean promotionActive = false;
    private Double prixPromotion;
    private Double reductionPourcentage;
    private Integer chambresDisponibles;
    private Integer nombreChambres;
    private String typeChambreRecommandee;
    private Double superficieMoyenne;
    private Boolean wifiGratuit = true;
    private Boolean petitDejeunerInclus = false;
    private Boolean annulationGratuite = false;
    private Boolean paiementSecurise = true;
    
    // Méthodes utilitaires
    public String getEtoilesDisplay() {
        if (etoiles == null) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < etoiles; i++) {
            sb.append("★");
        }
        return sb.toString();
    }
    
    public String getNoteFormatee() {
        if (noteMoyenne == null) return "N/A";
        return String.format("%.1f", noteMoyenne);
    }
    
    public String getPrixFormate() {
        if (promotionActive && prixPromotion != null) {
            return String.format("À partir de %.0f€/nuit", prixPromotion);
        }
        if (prixMinimum != null) {
            return String.format("À partir de %.0f€/nuit", prixMinimum);
        }
        return "Prix sur demande";
    }
    
    public String getReductionFormatee() {
        if (reductionPourcentage != null && promotionActive) {
            return String.format("-%.0f%%", reductionPourcentage);
        }
        return "";
    }
    
    public String getLocalisationComplete() {
        if (quartier != null && !quartier.isEmpty()) {
            return quartier + ", " + ville + ", " + pays;
        }
        return ville + ", " + pays;
    }
}