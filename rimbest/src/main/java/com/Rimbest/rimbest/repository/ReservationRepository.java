package com.Rimbest.rimbest.repository;

import com.Rimbest.rimbest.model.Chambre;
import com.Rimbest.rimbest.model.Reservation;
import com.Rimbest.rimbest.model.StatutReservation;
import com.Rimbest.rimbest.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;



import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

        // Trouver par client
        List<Reservation> findByClient(User client);

        Page<Reservation> findByClient(User client, Pageable pageable);

        // Trouver par hôtel
        @Query("SELECT r FROM Reservation r WHERE r.chambre.hotel.id = :hotelId")
        List<Reservation> findByHotelId(@Param("hotelId") Long hotelId);

        @Query("SELECT r FROM Reservation r WHERE r.chambre.hotel.id = :hotelId")
        Page<Reservation> findByHotelId(@Param("hotelId") Long hotelId, Pageable pageable);

        // Trouver par partenaire
        @Query("SELECT r FROM Reservation r WHERE r.chambre.hotel.partenaire = :partenaire")
        List<Reservation> findByPartenaire(@Param("partenaire") User partenaire);

        @Query("SELECT r FROM Reservation r WHERE r.chambre.hotel.partenaire = :partenaire")
        Page<Reservation> findByPartenaire(@Param("partenaire") User partenaire, Pageable pageable);

        // Trouver par chambre
        List<Reservation> findByChambre(Chambre chambre);

        // trouver par chambre et date arrivée après
        @Query("SELECT r FROM Reservation r WHERE r.chambre = :chambre AND r.dateArrivee >= :dateNow AND r.statut IN ('CONFIRMEE', 'EN_ATTENTE')")
        List<Reservation> findByChambreAndDateArriveeAfter(@Param("chambre") Chambre chambre,
                        @Param("dateNow") LocalDate dateNow);

        // Trouver par statut
        List<Reservation> findByStatut(StatutReservation statut);

        // Trouver par statut et date depart avant
        List<Reservation> findByStatutAndDateDepartBefore(StatutReservation statut, LocalDate dateDepart);

        // Trouver par période
        @Query("SELECT r FROM Reservation r WHERE " +
                        "(r.dateArrivee <= :dateFin AND r.dateDepart >= :dateDebut) " +
                        "AND r.statut IN ('CONFIRMEE', 'EN_ATTENTE')")
        List<Reservation> findByDateRange(
                        @Param("dateDebut") LocalDate dateDebut,
                        @Param("dateFin") LocalDate dateFin);

        // Vérifier disponibilité chambre
        @Query("SELECT COUNT(r) > 0 FROM Reservation r WHERE " +
                        "r.chambre.id = :chambreId AND " +
                        "r.statut IN ('CONFIRMEE', 'EN_ATTENTE') AND " +
                        "(r.dateArrivee < :dateDepart AND r.dateDepart > :dateArrivee)")
        boolean isChambreReservee(
                        @Param("chambreId") Long chambreId,
                        @Param("dateArrivee") LocalDate dateArrivee,
                        @Param("dateDepart") LocalDate dateDepart);

        // Trouver les réservations en conflit pour une chambre et période donnée
        @Query("SELECT r FROM Reservation r WHERE " +
                        "r.chambre.id = :chambreId AND " +
                        "r.statut IN ('CONFIRMEE', 'EN_ATTENTE') AND " +
                        "(r.dateArrivee < :dateDepart AND r.dateDepart > :dateArrivee)")
        List<Reservation> findByChambreAndDateRange(
                        @Param("chambreId") Long chambreId,
                        @Param("dateArrivee") LocalDate dateArrivee,
                        @Param("dateDepart") LocalDate dateDepart);

        // Statistiques
        @Query("SELECT COUNT(r) FROM Reservation r WHERE r.chambre.hotel.id = :hotelId")
        long countByHotel(@Param("hotelId") Long hotelId);

        @Query("SELECT COUNT(r) FROM Reservation r WHERE r.chambre.hotel.id = :hotelId AND r.statut = :statut")
        long countByHotelAndStatut(@Param("hotelId") Long hotelId, @Param("statut") StatutReservation statut);

        @Query("SELECT COALESCE(SUM(r.prixTotal), 0) FROM Reservation r WHERE " +
                        "r.chambre.hotel.id = :hotelId AND " +
                        "r.statut = 'CONFIRMEE' AND " +
                        "YEAR(r.dateArrivee) = :year AND MONTH(r.dateArrivee) = :month")
        BigDecimal getRevenueByHotelAndMonth(
                        @Param("hotelId") Long hotelId,
                        @Param("year") int year,
                        @Param("month") int month);

        // Taux d'occupation
        @Query("SELECT COUNT(DISTINCT r.chambre.id) FROM Reservation r WHERE " +
                        "r.chambre.hotel.id = :hotelId AND " +
                        "r.statut = 'CONFIRMEE' AND " +
                        "r.dateArrivee >= :startDate AND r.dateArrivee <= :endDate")
        Integer countChambresOccupeesByHotelInPeriod(
                        @Param("hotelId") Long hotelId,
                        @Param("startDate") LocalDate startDate,
                        @Param("endDate") LocalDate endDate);

        // Trouver par référence
        Optional<Reservation> findByReference(String reference);

        // Réservations récentes
        Page<Reservation> findAllByOrderByDateArriveeDesc(Pageable pageable);

        Page<Reservation> findByClientId(Long clientId, Pageable pageable);

        long countByStatut(StatutReservation statut);

        @Query("SELECT COALESCE(SUM(r.prixTotal), 0) FROM Reservation r WHERE r.statut = 'CONFIRMEE'")
        BigDecimal getTotalRevenue();

        @Query("SELECT COALESCE(SUM(r.prixTotal), 0) FROM Reservation r WHERE r.chambre.hotel.partenaire = :partenaire AND r.statut = 'CONFIRMEE'")
        BigDecimal getTotalRevenueByPartenaire(@Param("partenaire") User partenaire);

        @Query("SELECT COALESCE(SUM(r.prixTotal), 0) FROM Reservation r WHERE r.chambre.hotel.partenaire = :partenaire AND r.statut = 'CONFIRMEE' AND YEAR(r.dateArrivee) = :year AND MONTH(r.dateArrivee) = :month")
        BigDecimal getRevenueByPartenaireAndMonth(@Param("partenaire") User partenaire, @Param("year") int year,
                        @Param("month") int month);

        @Modifying
        @Query("UPDATE Reservation r SET r.statut = :statut WHERE r.id = :id")
        int updateStatut(@Param("id") Long id, @Param("statut") StatutReservation statut);

}