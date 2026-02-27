package com.Rimbest.rimbest.service;

import com.Rimbest.rimbest.model.dto.PaiementDTO;
import com.Rimbest.rimbest.model.Paiement;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface PaiementService {
    
    Paiement createPaiement(Long reservationId, BigDecimal montant, String methode);
    Paiement validerPaiement(Long paiementId);
    Paiement annulerPaiement(Long paiementId);
    Paiement getPaiementById(Long id);
    List<Paiement> getPaiementsByReservation(Long reservationId);
    List<Paiement> getPaiementsByClient(Long clientId);
    List<Paiement> getPaiementsByHotel(Long hotelId);
    BigDecimal getRevenueByPeriod(LocalDate startDate, LocalDate endDate);
    PaiementDTO convertToDTO(Paiement paiement);
}