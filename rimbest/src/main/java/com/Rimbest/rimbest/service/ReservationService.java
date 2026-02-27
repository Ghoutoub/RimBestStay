package com.Rimbest.rimbest.service;

import com.Rimbest.rimbest.model.dto.ReservationDTO;
import com.Rimbest.rimbest.model.dto.ReservationRequestDTO;
import com.Rimbest.rimbest.model.dto.ReservationStatsDTO;
import com.Rimbest.rimbest.repository.ReservationRepository;
import com.Rimbest.rimbest.model.dto.DisponibiliteDTO;
import com.Rimbest.rimbest.model.Reservation;
import com.Rimbest.rimbest.model.StatutReservation;
import com.Rimbest.rimbest.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface ReservationService {

    // Création et gestion
    Reservation createReservation(ReservationRequestDTO requestDTO, User client);

    Reservation confirmReservation(Long reservationId);

    Reservation cancelReservation(Long reservationId, String raison);

    Reservation updateReservation(Long reservationId, ReservationRequestDTO requestDTO);

    Reservation refuseReservation(Long reservationId, String raison);

    void deleteReservation(Long reservationId);

    // Consultation
    Reservation getReservationById(Long id);

    Reservation getReservationByReference(String reference);

    List<Reservation> getReservationsByClient(User client);

    List<Reservation> getReservationsByHotel(Long hotelId);

    List<Reservation> getReservationsByPartenaire(User partenaire);

    Page<Reservation> getReservationsPage(Pageable pageable);

    // Disponibilité
    boolean checkDisponibilite(Long chambreId, LocalDate dateArrivee, LocalDate dateDepart);

    List<DisponibiliteDTO> getDisponibilitesChambre(Long chambreId, LocalDate dateDebut, LocalDate dateFin);

    List<DisponibiliteDTO> getDisponibilitesHotel(Long hotelId, LocalDate dateDebut, LocalDate dateFin);

    // Statistiques
    BigDecimal calculatePrixTotal(Long chambreId, LocalDate dateArrivee, LocalDate dateDepart, Integer personnes);

    long countReservationsByStatut(StatutReservation statut);

    long countReservationsByHotelAndStatut(Long hotelId, StatutReservation statut);

    BigDecimal getRevenueByHotelAndMonth(Long hotelId, int year, int month);

    double getTauxOccupationHotel(Long hotelId, LocalDate startDate, LocalDate endDate);

    // Conversion
    ReservationDTO convertToDTO(Reservation reservation);

    List<ReservationDTO> convertToDTOList(List<Reservation> reservations);

    // Recherche avancée
    List<Reservation> searchReservations(String keyword, LocalDate dateDebut, LocalDate dateFin,
            StatutReservation statut);

    Page<Reservation> getReservationsByClient(Long clientId, Pageable pageable);

    // Méthodes statistiques supplémentaires
    long countTotalReservations();

    long countReservationsToday();

    long countReservationsByClient(User client);

    BigDecimal getRevenueForCurrentMonth();

    BigDecimal getRevenueByClient(User client);

    List<Reservation> getRecentReservationsByClient(User client, int limit);

    ReservationStatsDTO getStats();

    ReservationStatsDTO getStats(User partenaire);
}