package com.Rimbest.rimbest.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

@Entity
@Table(name = "chambres")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Chambre {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Le numéro est obligatoire")
    @Size(max = 20)
    private String numero;
    
    @NotBlank(message = "Le type est obligatoire")
    @Size(max = 50)
    private String typeChambre; // SIMPLE, DOUBLE, SUITE, FAMILIALE, etc.
    
    @NotNull(message = "La capacité est obligatoire")
    @Min(value = 1, message = "La capacité doit être au moins 1")
    private Integer capacite;
    
    @NotNull(message = "Le prix est obligatoire")
    @DecimalMin(value = "0.0", inclusive = false, message = "Le prix doit être positif")
    private Double prixNuit;
    
    @Column(length = 500)
    private String description;
    
    @Column(name = "superficie")
    private Double superficie; // en m²
    
    // Équipements de la chambre
    @Column(name = "equipements_chambre", length = 500)
    private String equipementsChambre; // "TV,AC,MINIBAR,SAFE,TERRACE,SEAVIEW"
    
    // Vue
    @Column(name = "vue_type")
    private String vueType; // MER, MONTAGNE, VILLE, JARDIN, PISCINE
    
    // Nombre de lits
    @Column(name = "nombre_lits")
    private Integer nombreLits = 1;
    
    @Column(name = "type_lits")
    private String typeLits; // "1 grand lit", "2 lits simples", etc.
    
    @Column(name = "prix_weekend")
    private Double prixWeekend;
    
    @Column(name = "taxe_sejour")
    private Double taxeSejour = 20.0;
    
    @Column(name = "depot_garantie")
    private Double depotGarantie = 500.0;
    
    // Images de la chambre
    @Column(name = "images_chambre", length = 1000)
    private String imagesChambre;
    
    // Caractéristiques supplémentaires
    @Column(name = "salle_bain_privee")
    private Boolean salleBainPrivee = true;
    
    @Column(name = "climatisation")
    private Boolean climatisation = true;
    
    @Column(name = "television")
    private Boolean television = true;
    
    @Column(name = "wifi")
    private Boolean wifi = true;
    
    @Column(name = "minibar")
    private Boolean minibar = false;
    
    @Column(name = "coffre_fort")
    private Boolean coffreFort = false;
    
    private Boolean disponible = true;
    
    @Column(name = "statut_nettoyage")
    private String statutNettoyage = "PROPRE";
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hotel_id", nullable = false)
    private Hotel hotel;
    
    @OneToMany(mappedBy = "chambre", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Reservation> reservations = new ArrayList<>();
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Méthodes utilitaires
    public void addReservation(Reservation reservation) {
        reservations.add(reservation);
        reservation.setChambre(this);
    }
    
    public void removeReservation(Reservation reservation) {
        reservations.remove(reservation);
        reservation.setChambre(null);
    }
    
    // Vérifier si disponible pour une période
    // @Transient
    // public boolean isDisponiblePourPeriode(LocalDateTime debut, LocalDateTime fin) {
    //     if (!disponible) return false;
        
    //     if (reservations == null || reservations.isEmpty()) return true;
        
    //     return reservations.stream()
    //             .noneMatch(reservation -> reservation.estEnConflit(debut, fin));
    // }
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // Constructeur pour tests
    public Chambre(String numero, String typeChambre, Integer capacite, Double prixNuit, Hotel hotel) {
        this.numero = numero;
        this.typeChambre = typeChambre;
        this.capacite = capacite;
        this.prixNuit = prixNuit;
        this.hotel = hotel;
        this.disponible = true;
    }
    
    // Getters et Setters personnalisés pour Thymeleaf
    public boolean getDisponible() {
        return disponible != null && disponible;
    }
    
    public void setDisponible(boolean disponible) {
        this.disponible = disponible;
    }
    // Nouvelle méthode pour récupérer la première image de la chambre
    @Transient
    public String getFirstImageUrl() {
        if (imagesChambre != null && !imagesChambre.isEmpty()) {
            String[] urls = imagesChambre.split(",");
            return urls.length > 0 ? urls[0].trim() : "/images/chambre-default.jpg";
        }
        return "/images/chambre-default.jpg";
    }
    
    // Vérifier la disponibilité pour une période spécifique
    @Transient
    public boolean isDisponiblePourPeriode(LocalDateTime dateArrivee, LocalDateTime dateDepart) {
        if (!disponible) return false;
        
        // À implémenter avec la logique des réservations existantes
        return true;
    }

      @Transient
    private List<MultipartFile> imageFiles = new ArrayList<>();
    
    // Méthode utilitaire
    public void addImageUrl(String url) {
        if (this.imagesChambre == null || this.imagesChambre.isEmpty()) {
            this.imagesChambre = url;
        } else {
            this.imagesChambre += "," + url;
        }
    }
    
    @Transient
    public List<String> getImageList() {
        if (imagesChambre == null || imagesChambre.isEmpty()) {
            return new ArrayList<>();
        }
        return Arrays.asList(imagesChambre.split(","));
    }
}