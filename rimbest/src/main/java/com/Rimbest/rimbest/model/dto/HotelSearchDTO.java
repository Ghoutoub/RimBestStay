package com.Rimbest.rimbest.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HotelSearchDTO {
    
    // Localisation
    private String ville;
    private String pays;
    private String quartier;
    
    // Dates
    private LocalDate dateArrivee;
    private LocalDate dateDepart;
    
    // Personnes
    private Integer adultes = 1;
    private Integer enfants = 0;
    private Integer chambres = 1;
    
    // Budget
    private Double prixMin;
    private Double prixMax;
    
    // Classement
    private Integer etoilesMin;
    private Integer etoilesMax;
    
    // Équipements hôtel
    private List<String> equipementsHotel; // ["WIFI", "POOL", "SPA", "PARKING"]
    
    // Équipements chambre
    private List<String> equipementsChambre; // ["TV", "AC", "MINIBAR"]
    
    // Vue
    private String vueType;
    
    // Type de chambre
    private String typeChambre;
    
    // Capacité minimale
    private Integer capaciteMin;
    
    // Tri
    private String triPar = "recommandation"; // "prix_croissant", "prix_decroissant", "etoiles", "note"
    
    // Pagination
    private Integer page = 0;
    private Integer taille = 10;
    
    // Localisation GPS
    private Double latitude;
    private Double longitude;
    private Double rayon; // en km
    
    // Constructeur simplifié
    public HotelSearchDTO(String ville, LocalDate dateArrivee, LocalDate dateDepart, Integer adultes) {
        this.ville = ville;
        this.dateArrivee = dateArrivee;
        this.dateDepart = dateDepart;
        this.adultes = adultes;
    }
    
    // Méthodes utilitaires
    public boolean hasDates() {
        return dateArrivee != null && dateDepart != null;
    }
    
    public boolean hasLocation() {
        return (ville != null && !ville.isEmpty()) || 
               (latitude != null && longitude != null);
    }
    
    public int getTotalPersonnes() {
        return (adultes != null ? adultes : 0) + (enfants != null ? enfants : 0);
    }
}