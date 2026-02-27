package com.Rimbest.rimbest.service.impl;

import com.Rimbest.rimbest.model.dto.PaiementDTO;
import com.Rimbest.rimbest.model.Paiement;
import com.Rimbest.rimbest.model.Reservation;
import com.Rimbest.rimbest.repository.PaiementRepository;
import com.Rimbest.rimbest.repository.ReservationRepository;
import com.Rimbest.rimbest.service.PaiementService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaiementServiceImpl implements PaiementService {
    
    private final PaiementRepository paiementRepository;
    private final ReservationRepository reservationRepository;
    
    @Override
    @Transactional
    public Paiement createPaiement(Long reservationId, BigDecimal montant, String methode) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("Réservation non trouvée"));
        
        Paiement paiement = new Paiement();
        paiement.setReference("PAY-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        paiement.setMontant(montant);
        paiement.setMethodePaiement(methode);
        paiement.setStatut("EN_ATTENTE");
        paiement.setDatePaiement(LocalDateTime.now());
        paiement.setReservation(reservation);
        
        return paiementRepository.save(paiement);
    }
    
    @Override
    @Transactional
    public Paiement validerPaiement(Long paiementId) {
        Paiement paiement = paiementRepository.findById(paiementId)
                .orElseThrow(() -> new RuntimeException("Paiement non trouvé"));
        
        paiement.setStatut("VALIDE");
        paiement.setDatePaiement(LocalDateTime.now());
        
        // Si le paiement est pour une réservation, on peut confirmer la réservation
        if (paiement.getReservation() != null) {
            Reservation reservation = paiement.getReservation();
            if (reservation.getStatut().toString().equals("EN_ATTENTE")) {
                // Mettre à jour le statut de la réservation via le service de réservation
                // Cette partie sera complétée dans le ReservationController
            }
        }
        
        return paiementRepository.save(paiement);
    }
    
    @Override
    public List<Paiement> getPaiementsByReservation(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("Réservation non trouvée"));
        return paiementRepository.findByReservation(reservation);
    }
    
    @Override
    public BigDecimal getRevenueByPeriod(LocalDate startDate, LocalDate endDate) {
        return paiementRepository.getRevenueByPeriod(startDate, endDate);
    }
    
    @Override
    public List<Paiement> getPaiementsByHotel(Long hotelId) {
        return paiementRepository.findByHotelId(hotelId);
    }
    
    @Override
    public Paiement getPaiementById(Long id) {
        return paiementRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Paiement non trouvé"));
    }
    
    @Override
    @Transactional
    public Paiement annulerPaiement(Long paiementId) {
        Paiement paiement = paiementRepository.findById(paiementId)
                .orElseThrow(() -> new RuntimeException("Paiement non trouvé"));
        
        paiement.setStatut("ANNULE");
        
        return paiementRepository.save(paiement);
    }
    
    @Override
    public List<Paiement> getPaiementsByClient(Long clientId) {
        return paiementRepository.findByClientId(clientId);
    }
    
    @Override
    public PaiementDTO convertToDTO(Paiement paiement) {
        PaiementDTO dto = new PaiementDTO();
        dto.setId(paiement.getId());
        dto.setReference(paiement.getReference());
        dto.setMontant(paiement.getMontant());
        dto.setMethodePaiement(paiement.getMethodePaiement());
        dto.setStatut(paiement.getStatut());
        dto.setDatePaiement(paiement.getDatePaiement());
        
        if (paiement.getReservation() != null) {
            dto.setReservationId(paiement.getReservation().getId());
            dto.setReservationReference(paiement.getReservation().getReference());
            
            if (paiement.getReservation().getClient() != null) {
                dto.setClientEmail(paiement.getReservation().getClient().getEmail());
            }
        }
        
        return dto;
    }
}