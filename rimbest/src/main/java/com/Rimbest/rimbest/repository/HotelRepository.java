package com.Rimbest.rimbest.repository;

import com.Rimbest.rimbest.model.Hotel;
import com.Rimbest.rimbest.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface HotelRepository extends JpaRepository<Hotel, Long>, JpaSpecificationExecutor<Hotel> {

       // ============ MÉTHODES EXISTANTES ============
       List<Hotel> findByActifTrue();

       Page<Hotel> findByActifTrue(Pageable pageable);

       List<Hotel> findByVilleContainingIgnoreCase(String ville);

       List<Hotel> findByPays(String pays);

       List<Hotel> findByEtoiles(Integer etoiles);

       long countByActifTrue();

       boolean existsByNomAndVille(String nom, String ville);

       List<Hotel> findByPartenaire(User partenaire);

       Page<Hotel> findByPartenaire(User partenaire, Pageable pageable);

       List<Hotel> findByPartenaireAndActifTrue(User partenaire);

       // ============ NOUVELLES MÉTHODES POUR RECHERCHE AVANCÉE ============

       // Recherche avec spécifications
       Page<Hotel> findAll(org.springframework.data.jpa.domain.Specification<Hotel> spec, Pageable pageable);

       // Recherche par disponibilité
       @Query("SELECT DISTINCT h FROM Hotel h " +
                     "JOIN h.chambres c " +
                     "WHERE h.actif = true " +
                     "AND c.disponible = true " +
                     "AND c.capacite >= :capacite " +
                     "AND (:ville IS NULL OR LOWER(h.ville) LIKE LOWER(CONCAT('%', :ville, '%'))) " +
                     "AND (:pays IS NULL OR LOWER(h.pays) LIKE LOWER(CONCAT('%', :pays, '%'))) " +
                     "AND (:etoilesMin IS NULL OR h.etoiles >= :etoilesMin) " +
                     "AND (:etoilesMax IS NULL OR h.etoiles <= :etoilesMax) " +
                     "AND (:prixMin IS NULL OR c.prixNuit >= :prixMin) " +
                     "AND (:prixMax IS NULL OR c.prixNuit <= :prixMax) " +
                     "AND (:equipement IS NULL OR h.equipementsHotel LIKE LOWER(CONCAT('%', :equipement, '%')))")
       Page<Hotel> findAvailableHotels(
                     @Param("ville") String ville,
                     @Param("pays") String pays,
                     @Param("etoilesMin") Integer etoilesMin,
                     @Param("etoilesMax") Integer etoilesMax,
                     @Param("prixMin") Double prixMin,
                     @Param("prixMax") Double prixMax,
                     @Param("capacite") Integer capacite,
                     @Param("equipement") String equipement,
                     Pageable pageable);

       // Recherche par équipements
       @Query("SELECT h FROM Hotel h " +
                     "WHERE h.actif = true " +
                     "AND (:equipement IS NULL OR h.equipementsHotel LIKE CONCAT('%', :equipement, '%'))")
       List<Hotel> findByEquipement(@Param("equipement") String equipement, Pageable pageable);

       // Recherche avec plusieurs équipements
       @Query("SELECT h FROM Hotel h " +
                     "WHERE h.actif = true " +
                     "AND h.equipementsHotel LIKE %:equipement1% " +
                     "AND h.equipementsHotel LIKE %:equipement2%")
       List<Hotel> findByMultipleEquipements(
                     @Param("equipement1") String equipement1,
                     @Param("equipement2") String equipement2,
                     Pageable pageable);

       // Recherche par note minimale
       List<Hotel> findByNoteMoyenneGreaterThanEqual(Double noteMin);

       Page<Hotel> findByNoteMoyenneGreaterThanEqual(Double noteMin, Pageable pageable);

       // Recherche par géolocalisation
       @Query("SELECT h FROM Hotel h " +
                     "WHERE h.actif = true " +
                     "AND h.latitude IS NOT NULL " +
                     "AND h.longitude IS NOT NULL " +
                     "AND (6371 * acos(cos(radians(:latitude)) * cos(radians(h.latitude)) * " +
                     "cos(radians(h.longitude) - radians(:longitude)) + sin(radians(:latitude)) * " +
                     "sin(radians(h.latitude)))) <= :radius")
       List<Hotel> findByLocationWithinRadius(
                     @Param("latitude") Double latitude,
                     @Param("longitude") Double longitude,
                     @Param("radius") Double radius);

       // Recherche par type de chambre
       @Query("SELECT DISTINCT h FROM Hotel h " +
                     "JOIN h.chambres c " +
                     "WHERE h.actif = true " +
                     "AND c.disponible = true " +
                     "AND LOWER(c.typeChambre) = LOWER(:typeChambre)")
       List<Hotel> findByTypeChambre(@Param("typeChambre") String typeChambre);

       // Statistiques de recherche
       @Query("SELECT COUNT(DISTINCT h.ville) FROM Hotel h WHERE h.actif = true")
       long countVillesDisponibles();

       @Query("SELECT DISTINCT h.ville FROM Hotel h WHERE h.ville IS NOT NULL ORDER BY h.ville")
       List<String> findDistinctVilles();

       @Query("SELECT DISTINCT h.pays FROM Hotel h WHERE h.pays IS NOT NULL ORDER BY h.pays")
       List<String> findDistinctPays();

       @Query("SELECT DISTINCT c.typeChambre FROM Chambre c ORDER BY c.typeChambre")
       List<String> findDistinctTypesChambre();

       // Prix minimum et maximum
       @Query("SELECT MIN(c.prixNuit) FROM Chambre c WHERE c.disponible = true")
       Optional<Double> findMinPrice();

       @Query("SELECT MAX(c.prixNuit) FROM Chambre c WHERE c.disponible = true")
       Optional<Double> findMaxPrice();

       // Destinations populaires
       @Query("SELECT h.ville, COUNT(h) as count FROM Hotel h " +
                     "WHERE h.actif = true " +
                     "GROUP BY h.ville " +
                     "ORDER BY count DESC")
       List<Object[]> findTopDestinations(Pageable pageable);

       // Recherche existante (à garder pour compatibilité)
       @Query("SELECT h FROM Hotel h WHERE " +
                     "(:keyword IS NULL OR :keyword = '' OR " +
                     "LOWER(h.nom) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
                     "LOWER(h.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
                     "(:ville IS NULL OR :ville = '' OR LOWER(h.ville) LIKE LOWER(CONCAT('%', :ville, '%'))) AND " +
                     "(:etoiles IS NULL OR h.etoiles = :etoiles) AND " +
                     "(:actif IS NULL OR h.actif = :actif) " +
                     "ORDER BY h.createdAt DESC")
       List<Hotel> searchHotels(
                     @Param("keyword") String keyword,
                     @Param("ville") String ville,
                     @Param("etoiles") Integer etoiles,
                     @Param("actif") Boolean actif);

       @Query("SELECT h FROM Hotel h WHERE " +
                     "(:keyword IS NULL OR :keyword = '' OR " +
                     "LOWER(h.nom) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
                     "LOWER(h.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
                     "(:ville IS NULL OR :ville = '' OR LOWER(h.ville) LIKE LOWER(CONCAT('%', :ville, '%'))) AND " +
                     "(:etoiles IS NULL OR h.etoiles = :etoiles) AND " +
                     "(:actif IS NULL OR h.actif = :actif) " +
                     "ORDER BY h.createdAt DESC")
       Page<Hotel> searchHotelsPage(
                     @Param("keyword") String keyword,
                     @Param("ville") String ville,
                     @Param("etoiles") Integer etoiles,
                     @Param("actif") Boolean actif,
                     Pageable pageable);

       @Query("SELECT DISTINCT h.ville FROM Hotel h WHERE h.ville IS NOT NULL ORDER BY h.ville")
       List<String> findAllVillesDistinct();

       @Query("SELECT DISTINCT h.pays FROM Hotel h WHERE h.pays IS NOT NULL ORDER BY h.pays")
       List<String> findAllPaysDistinct();

       @Query("SELECT COUNT(h) FROM Hotel h WHERE h.partenaire = :partenaire")
       long countByPartenaire(@Param("partenaire") User partenaire);

       @Query("SELECT COUNT(h) FROM Hotel h WHERE h.partenaire = :partenaire AND h.actif = true")
       long countByPartenaireAndActifTrue(@Param("partenaire") User partenaire);

       List<Hotel> findAllByOrderByCreatedAtDesc(Pageable pageable);

       @Query("SELECT h FROM Hotel h WHERE h.actif = true ORDER BY SIZE(h.chambres) DESC")
       List<Hotel> findTopHotelsByChambresCount(Pageable pageable);

       @Query("SELECT h FROM Hotel h WHERE h.actif = true AND SIZE(h.chambres) > 0 ORDER BY h.etoiles DESC, h.nom")
       List<Hotel> findRecommendedHotels(Pageable pageable);

       // Ajouter ces méthodes à la fin de HotelRepository.java

       @Query("SELECT DISTINCT h.ville FROM Hotel h " +
                     "WHERE LOWER(h.ville) LIKE LOWER(CONCAT('%', :query, '%')) " +
                     "ORDER BY h.ville")
       List<String> findCitySuggestions(@Param("query") String query);

       @Query("SELECT DISTINCT h.nom FROM Hotel h " +
                     "WHERE LOWER(h.nom) LIKE LOWER(CONCAT('%', :query, '%')) " +
                     "AND h.actif = true " +
                     "ORDER BY h.nom")
       List<String> findHotelNameSuggestions(@Param("query") String query);

       @Query("SELECT h.ville, COUNT(h) FROM Hotel h " +
                     "WHERE h.actif = true " +
                     "GROUP BY h.ville " +
                     "ORDER BY COUNT(h) DESC")
       List<Object[]> countHotelsByCity();

       @Query("SELECT h.etoiles, COUNT(h) FROM Hotel h " +
                     "WHERE h.actif = true AND h.etoiles IS NOT NULL " +
                     "GROUP BY h.etoiles " +
                     "ORDER BY h.etoiles DESC")
       List<Object[]> countHotelsByStars();

       @EntityGraph(attributePaths = { "chambres", "partenaire" })
       @Query("SELECT h FROM Hotel h LEFT JOIN FETCH h.chambres LEFT JOIN FETCH h.partenaire WHERE h.id = :id")
       Optional<Hotel> findByIdWithChambres(@Param("id") Long id);

}
