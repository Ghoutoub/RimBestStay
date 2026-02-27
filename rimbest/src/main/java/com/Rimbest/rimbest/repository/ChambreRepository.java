package com.Rimbest.rimbest.repository;

import com.Rimbest.rimbest.model.Chambre;
import com.Rimbest.rimbest.model.Hotel;
import com.Rimbest.rimbest.model.User;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ChambreRepository extends JpaRepository<Chambre, Long> {

        // ============ MÉTHODES DE BASE ============

        List<Chambre> findByHotel(Hotel hotel);

        List<Chambre> findByHotelAndDisponibleTrue(Hotel hotel);

        List<Chambre> findByTypeChambre(String typeChambre);

        boolean existsByHotelAndNumero(Hotel hotel, String numero);

        long countByHotel(Hotel hotel);

        long countByHotelAndDisponibleTrue(Hotel hotel);

        // ============ MÉTHODES DE RECHERCHE AVANCÉE ============

        @Query("SELECT c FROM Chambre c WHERE c.hotel.id = :hotelId AND " +
                        "(:search IS NULL OR :search = '' OR " +
                        "LOWER(c.numero) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
                        "LOWER(c.description) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
                        "(:typeChambre IS NULL OR :typeChambre = '' OR c.typeChambre = :typeChambre) AND " +
                        "(:minPrice IS NULL OR c.prixNuit >= :minPrice) AND " +
                        "(:maxPrice IS NULL OR c.prixNuit <= :maxPrice) AND " +
                        "(:disponible IS NULL OR c.disponible = :disponible) " +
                        "ORDER BY c.numero")
        Page<Chambre> searchChambresPage(
                        @Param("hotelId") Long hotelId,
                        @Param("search") String search,
                        @Param("typeChambre") String typeChambre,
                        @Param("minPrice") Double minPrice,
                        @Param("maxPrice") Double maxPrice,
                        @Param("disponible") Boolean disponible,
                        Pageable pageable);

        @Query("SELECT c FROM Chambre c WHERE c.hotel.id = :hotelId AND " +
                        "(:search IS NULL OR :search = '' OR " +
                        "LOWER(c.numero) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
                        "LOWER(c.description) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
                        "(:typeChambre IS NULL OR :typeChambre = '' OR c.typeChambre = :typeChambre) AND " +
                        "(:minPrice IS NULL OR c.prixNuit >= :minPrice) AND " +
                        "(:maxPrice IS NULL OR c.prixNuit <= :maxPrice) AND " +
                        "(:disponible IS NULL OR c.disponible = :disponible) " +
                        "ORDER BY c.numero")
        List<Chambre> searchChambres(
                        @Param("hotelId") Long hotelId,
                        @Param("search") String search,
                        @Param("typeChambre") String typeChambre,
                        @Param("minPrice") Double minPrice,
                        @Param("maxPrice") Double maxPrice,
                        @Param("disponible") Boolean disponible);

        // ============ MÉTHODES STATISTIQUES ============

        @Query("SELECT AVG(c.prixNuit) FROM Chambre c WHERE c.hotel = :hotel")
        Double findAveragePriceByHotel(@Param("hotel") Hotel hotel);

        @Query("SELECT MIN(c.prixNuit) FROM Chambre c WHERE c.hotel = :hotel")
        Double findMinPriceByHotel(@Param("hotel") Hotel hotel);

        @Query("SELECT MAX(c.prixNuit) FROM Chambre c WHERE c.hotel = :hotel")
        Double findMaxPriceByHotel(@Param("hotel") Hotel hotel);

        // ============ MÉTHODES DE LISTE ============

        @Query("SELECT DISTINCT c.typeChambre FROM Chambre c WHERE c.typeChambre IS NOT NULL ORDER BY c.typeChambre")
        List<String> findAllTypesChambreDistinct();

        // Trouver les chambres disponibles pour un hôtel et une période
        @Query("SELECT c FROM Chambre c " +
                        "WHERE c.hotel.id = :hotelId " +
                        "AND c.disponible = true " +
                        "AND c.capacite >= :capacity " +
                        "AND (:roomType IS NULL OR c.typeChambre = :roomType) " +
                        "AND NOT EXISTS (" +
                        "  SELECT r FROM Reservation r " +
                        "  WHERE r.chambre = c " +
                        "  AND r.statut NOT IN ('ANNULEE', 'TERMINEE') " +
                        "  AND (" +
                        "    (r.dateArrivee < :departure AND r.dateDepart > :arrival)" +
                        "  )" +
                        ")")
        List<Chambre> findAvailableRooms(
                        @Param("hotelId") Long hotelId,
                        @Param("arrival") LocalDate arrival,
                        @Param("departure") LocalDate departure,
                        @Param("capacity") Integer capacity,
                        @Param("roomType") String roomType,
                        @Param("equipments") List<String> equipments);

        // Version simplifiée
        @Query("SELECT c FROM Chambre c " +
                        "WHERE c.hotel.id = :hotelId " +
                        "AND c.disponible = true " +
                        "AND c.capacite >= :capacity")
        List<Chambre> findAvailableRoomsSimple(
                        @Param("hotelId") Long hotelId,
                        @Param("capacity") Integer capacity);

        // Trouver les chambres par hôtel
        List<Chambre> findByHotelId(Long hotelId);

        Page<Chambre> findByHotelId(Long hotelId, Pageable pageable);

        @Query("SELECT c FROM Chambre c WHERE c.hotel.partenaire = :partenaire")
        Page<Chambre> findByPartenaire(@Param("partenaire") User partenaire, Pageable pageable);

        // Compter les chambres disponibles par hôtel
        @Query("SELECT COUNT(c) FROM Chambre c WHERE c.hotel.id = :hotelId AND c.disponible = true")
        long countAvailableRoomsByHotelId(@Param("hotelId") Long hotelId);

        // Trouver le prix minimum par hôtel
        @Query("SELECT MIN(c.prixNuit) FROM Chambre c WHERE c.hotel.id = :hotelId AND c.disponible = true")
        Double findMinPriceByHotelId(@Param("hotelId") Long hotelId);

        // Trouver le prix maximum par hôtel
        @Query("SELECT MAX(c.prixNuit) FROM Chambre c WHERE c.hotel.id = :hotelId AND c.disponible = true")
        Double findMaxPriceByHotelId(@Param("hotelId") Long hotelId);
}