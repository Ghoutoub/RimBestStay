package com.Rimbest.rimbest.repository;

import com.Rimbest.rimbest.model.Paiement;
import com.Rimbest.rimbest.model.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaiementRepository extends JpaRepository<Paiement, Long> {
    
    List<Paiement> findByReservation(Reservation reservation);
    
    @Query("SELECT p FROM Paiement p WHERE p.reservation.client.id = :clientId")
    List<Paiement> findByClientId(@Param("clientId") Long clientId);
    
    @Query("SELECT p FROM Paiement p WHERE p.reservation.chambre.hotel.id = :hotelId")
    List<Paiement> findByHotelId(@Param("hotelId") Long hotelId);
    
    Optional<Paiement> findByReference(String reference);
    
    @Query("SELECT COALESCE(SUM(p.montant), 0) FROM Paiement p WHERE " +
           "p.statut = 'VALIDE' AND " +
           "DATE(p.datePaiement) BETWEEN :startDate AND :endDate")
    BigDecimal getRevenueByPeriod(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
    
    List<Paiement> findByStatut(String statut);
    
    @Query("SELECT COUNT(p) FROM Paiement p WHERE " +
           "p.reservation.chambre.hotel.id = :hotelId AND " +
           "p.statut = 'VALIDE' AND " +
           "YEAR(p.datePaiement) = :year AND MONTH(p.datePaiement) = :month")
    long countValidPaymentsByHotelAndMonth(
            @Param("hotelId") Long hotelId,
            @Param("year") int year,
            @Param("month") int month);
}