package com.Rimbest.rimbest.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

@Entity
@Table(name = "hotels")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Hotel {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Le nom est obligatoire")
    @Size(max = 100)
    private String nom;
    
    @NotBlank(message = "La ville est obligatoire")
    @Size(max = 100)
    private String ville;
    
    @NotBlank(message = "Le pays est obligatoire")
    @Size(max = 100)
    private String pays;
    
    @Size(max = 100)
    private String quartier; // Nouveau
    
    @Min(value = 1, message = "Le nombre d'étoiles doit être entre 1 et 5")
    @Max(value = 5, message = "Le nombre d'étoiles doit être entre 1 et 5")
    private Integer etoiles;
    
    @Column(length = 500)
    private String description;
    
    private String adresse;
    private String telephone;
    private String email;
    
    // Équipements de l'hôtel (stockés en JSON ou chaîne séparée par virgules)
    @Column(name = "equipements_hotel", length = 500)
    private String equipementsHotel; // "WIFI,POOL,SPA,PARKING,RESTAURANT,GYM"
    
    // Coordonnées GPS
    private Double latitude;
    private Double longitude;
    
    // Note moyenne (calculée à partir des avis)
    @Column(name = "note_moyenne")
    private Double noteMoyenne = 0.0;
    
    @Column(name = "nombre_avis")
    private Integer nombreAvis = 0;
    
    // Images (stockées sous forme d'URLs séparées par des virgules)
    @Column(name = "images_urls", length = 1000)
    private String imagesUrls;
    
    // Prix indicatif (pour recherche rapide)
    @Column(name = "prix_minimum_indication")
    private Double prixMinimumIndication;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "partenaire_id")
    private User partenaire;
    
    private Boolean actif = true;
    
    @OneToMany(mappedBy = "hotel", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Chambre> chambres = new ArrayList<>();
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    // Méthodes utilitaires
    public void addChambre(Chambre chambre) {
        chambres.add(chambre);
        chambre.setHotel(this);
    }
    
    public void removeChambre(Chambre chambre) {
        chambres.remove(chambre);
        chambre.setHotel(null);
    }
    
    // Nombre de chambres (méthode de commodité)
    @Transient
    public int getNombreChambres() {
        return chambres != null ? chambres.size() : 0;
    }
    
    @Transient
    public int getNombreChambresDisponibles() {
        if (chambres == null) return 0;
        return (int) chambres.stream()
                .filter(Chambre::getDisponible)
                .count();
    }
    
    // Prix minimum (méthode de commodité)
    @Transient
    public Double getPrixMinimum() {
        if (chambres == null || chambres.isEmpty()) return 0.0;
        return chambres.stream()
                .mapToDouble(Chambre::getPrixNuit)
                .min()
                .orElse(0.0);
    }
    
    // Prix maximum (méthode de commodité)
    @Transient
    public Double getPrixMaximum() {
        if (chambres == null || chambres.isEmpty()) return 0.0;
        return chambres.stream()
                .mapToDouble(Chambre::getPrixNuit)
                .max()
                .orElse(0.0);
    }

    // Champs additionnels pour affichage (transients pour éviter migration DB)
    @Transient
    private String imageUrl;

        // Ajouter après le champ imagesUrls
    @Transient
    private List<MultipartFile> imageFiles = new ArrayList<>();
    
    @Transient
    private MultipartFile mainImageFile;
    
    // Méthode utilitaire pour gérer les images
    public void addImageUrl(String url) {
        if (this.imagesUrls == null || this.imagesUrls.isEmpty()) {
            this.imagesUrls = url;
        } else {
            this.imagesUrls += "," + url;
        }
    }
    
    // Récupérer toutes les images sous forme de liste
    @Transient
    public List<String> getImageList() {
        if (imagesUrls == null || imagesUrls.isEmpty()) {
            return new ArrayList<>();
        }
        return Arrays.asList(imagesUrls.split(","));
    }

    @Transient
    private Double ratingAverage;

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Double getRatingAverage() {
        return ratingAverage;
    }

    public void setRatingAverage(Double ratingAverage) {
        this.ratingAverage = ratingAverage;
    }
    
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
    public Hotel(String nom, String ville, String pays, Integer etoiles) {
        this.nom = nom;
        this.ville = ville;
        this.pays = pays;
        this.etoiles = etoiles;
        this.actif = true;
    }
 @Transient
    public String getFirstImageUrl() {
        if (imagesUrls != null && !imagesUrls.isEmpty()) {
            String[] urls = imagesUrls.split(",");
            return urls.length > 0 ? urls[0].trim() : "/images/hotel-default.jpg";
        }
        return "/images/hotel-default.jpg";
    }
    
    // Nouvelle méthode pour récupérer toutes les images
    @Transient
    public List<String> getAllImageUrls() {
        List<String> urls = new ArrayList<>();
        if (imagesUrls != null && !imagesUrls.isEmpty()) {
            for (String url : imagesUrls.split(",")) {
                urls.add(url.trim());
            }
        } else {
            urls.add("/images/hotel-default.jpg");
        }
        return urls;
    }
    
    // Vérifier si l'hôtel possède un équipement spécifique
    @Transient
    public boolean hasEquipement(String equipement) {
        if (equipementsHotel == null) return false;
        String[] equipements = equipementsHotel.split(",");
        for (String eq : equipements) {
            if (eq.trim().equalsIgnoreCase(equipement)) {
                return true;
            }
        }
        return false;
    }
}