package com.Rimbest.rimbest.model.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class PaiementDTO {
    private Long id;
    private String reference;
    private BigDecimal montant;
    private String methodePaiement; // CARTE, ESPECES, VIREMENT, CHEQUE
    private String statut; // EN_ATTENTE, VALIDE, ECHEC, REMBOURSE
    private LocalDateTime datePaiement;
    private String reservationReference;
    private Long reservationId;
    private String clientEmail;
}