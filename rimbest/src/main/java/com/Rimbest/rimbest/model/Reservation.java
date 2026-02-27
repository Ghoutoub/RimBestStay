package com.Rimbest.rimbest.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

@Entity
@Table(name = "reservations")
@Getter
@Setter
public class Reservation {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank
    @Column(unique = true)
    private String reference;
    
    @NotNull
    @FutureOrPresent(message = "La date d'arrivée doit être dans le présent ou le futur")
    private LocalDate dateArrivee;
    
    @NotNull
    @Future(message = "La date de départ doit être dans le futur")
    private LocalDate dateDepart;
    
    @Min(1)
    private Integer nombrePersonnes = 1;
    
    @Enumerated(EnumType.STRING)
    private StatutReservation statut = StatutReservation.EN_ATTENTE;
    
    @NotNull
    @DecimalMin(value = "0.0", inclusive = false)
    private BigDecimal prixTotal;
    
    @DecimalMin(value = "0.0", inclusive = true)
    private BigDecimal reduction = BigDecimal.ZERO;
    
    @DecimalMin(value = "0.0", inclusive = true)
    private BigDecimal depotGarantie = BigDecimal.ZERO;
    
    private String modePaiement;
    @Enumerated(EnumType.STRING)
    private StatutPaiement statutPaiement = StatutPaiement.EN_ATTENTE;

    
    @ManyToOne
    @JoinColumn(name = "client_id", nullable = false)
    private User client;
    
    @ManyToOne
    @JoinColumn(name = "chambre_id", nullable = false)
    private Chambre chambre;
    
    @Column(name = "created_at", updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;
    
    @Column(name = "updated_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedAt;
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getReference() { return reference; }
    public void setReference(String reference) { this.reference = reference; }
    
    public LocalDate getDateArrivee() { return dateArrivee; }
    public void setDateArrivee(LocalDate dateArrivee) { this.dateArrivee = dateArrivee; }
    
    public LocalDate getDateDepart() { return dateDepart; }
    public void setDateDepart(LocalDate dateDepart) { this.dateDepart = dateDepart; }
    
    public Integer getNombrePersonnes() { return nombrePersonnes; }
    public void setNombrePersonnes(Integer nombrePersonnes) { this.nombrePersonnes = nombrePersonnes; }
    
    public StatutReservation getStatut() { return statut; }
    public void setStatut(StatutReservation statut) { this.statut = statut; }
    
    public BigDecimal getPrixTotal() { return prixTotal; }
    public void setPrixTotal(BigDecimal prixTotal) { this.prixTotal = prixTotal; }
    
    public BigDecimal getReduction() { return reduction; }
    public void setReduction(BigDecimal reduction) { this.reduction = reduction; }
    
    public BigDecimal getDepotGarantie() { return depotGarantie; }
    public void setDepotGarantie(BigDecimal depotGarantie) { this.depotGarantie = depotGarantie; }
    
    public String getModePaiement() { return modePaiement; }
    public void setModePaiement(String modePaiement) { this.modePaiement = modePaiement; }
    
    public StatutPaiement getStatutPaiement() { return statutPaiement; }
    public void setStatutPaiement(StatutPaiement statutPaiement) { this.statutPaiement = statutPaiement; }
    
    public User getClient() { return client; }
    public void setClient(User client) { this.client = client; }
    
    public Chambre getChambre() { return chambre; }
    public void setChambre(Chambre chambre) { this.chambre = chambre; }
    
    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
    
    public Date getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }
    
    // Constructeur pour générer une référence unique
    @PrePersist
    protected void onCreate() {
        // Generate reference if null
        if (this.reference == null) {
            this.reference = "RES-" + System.currentTimeMillis() + "-" + 
                           (int)(Math.random() * 1000);
        }
        // Set timestamps
        createdAt = new Date();
        updatedAt = new Date();
    }
    // Vérification de la cohérence des dates
    @AssertTrue(message = "La date de départ doit être après la date d'arrivée")
    public boolean isDatesValides() {
        return dateDepart != null && dateArrivee != null && 
               dateDepart.isAfter(dateArrivee);
    }
    

    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = new Date();
    }
}