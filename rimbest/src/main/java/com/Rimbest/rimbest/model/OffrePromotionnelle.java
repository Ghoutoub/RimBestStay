package com.Rimbest.rimbest.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "offres_promotionnelles")
@Getter
@Setter
public class OffrePromotionnelle {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank
    @Size(max = 100)
    private String nomOffre;
    
    @NotNull
    @FutureOrPresent
    private LocalDate dateDebut;
    
    @NotNull
    @Future
    private LocalDate dateFin;
    
    @NotNull
    @DecimalMin(value = "0.0", inclusive = false)
    private BigDecimal prixPromo;
    
    @Column(length = 500)
    private String description;
    
    @ManyToOne
    @JoinColumn(name = "chambre_id", nullable = false)
    private Chambre chambre;
    
    // Vérification de la cohérence des dates
    @AssertTrue(message = "La date de fin doit être après la date de début")
    public boolean isDatesValides() {
        return dateFin != null && dateDebut != null && 
               dateFin.isAfter(dateDebut);
    }
}