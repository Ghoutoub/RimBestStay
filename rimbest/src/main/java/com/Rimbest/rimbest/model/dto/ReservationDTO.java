package com.Rimbest.rimbest.model.dto;

import com.Rimbest.rimbest.model.StatutReservation;
import com.Rimbest.rimbest.model.StatutPaiement;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class ReservationDTO {
    private Long id;
    private String reference;
    private LocalDate dateDebut; // Changé de dateArrivee
    private LocalDate dateFin; // Changé de dateDepart
    private Integer nbPersonnes; // Changé de nombrePersonnes
    private BigDecimal prixTotal;
    private StatutReservation statut;
    private String clientEmail;
    private String clientNom;
    private String clientTelephone;
    private String hotelNom;
    private String chambreNumero;
    private String chambreType;
    private Long chambreId;
    private Long hotelId;
    private Long clientId;

    private BigDecimal depotGarantie;
    private BigDecimal reduction;
    private String modePaiement;
    private StatutPaiement statutPaiement;
}