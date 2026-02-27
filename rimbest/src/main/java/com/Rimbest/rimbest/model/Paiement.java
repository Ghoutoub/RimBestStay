package com.Rimbest.rimbest.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "paiements")
@Getter
@Setter
public class Paiement {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank
    @Column(unique = true)
    private String reference;
    
    @NotNull
    @DecimalMin(value = "0.0", inclusive = false)
    private BigDecimal montant;
    
    @NotBlank
    private String methodePaiement; // CARTE, ESPECES, VIREMENT, CHEQUE
    
    @NotBlank
    private String statut; // EN_ATTENTE, VALIDE, ECHEC, REMBOURSE
    
    private LocalDateTime datePaiement;
    
    @Column(length = 500)
    private String details;
    
    @ManyToOne
    @JoinColumn(name = "reservation_id", nullable = false)
    private Reservation reservation;
    
    @PrePersist
    public void generateReference() {
        if (this.reference == null) {
            this.reference = "PAY-" + System.currentTimeMillis() + "-" + 
                           (int)(Math.random() * 1000);
        }
        if (this.datePaiement == null) {
            this.datePaiement = LocalDateTime.now();
        }
    }
}