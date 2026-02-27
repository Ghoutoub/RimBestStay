package com.Rimbest.rimbest.utils;

import com.Rimbest.rimbest.model.Hotel;
import com.Rimbest.rimbest.model.Chambre;
import com.Rimbest.rimbest.model.dto.HotelSearchDTO;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class SearchSpecification {

    public static Specification<Hotel> buildSearchSpecification(HotelSearchDTO searchDTO) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Joindre avec les chambres
            Join<Hotel, Chambre> chambresJoin = root.join("chambres", JoinType.INNER);
            
            // 1. Filtre par ville (optionnel)
            if (searchDTO.getVille() != null && !searchDTO.getVille().isEmpty()) {
                predicates.add(criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("ville")),
                    "%" + searchDTO.getVille().toLowerCase() + "%"
                ));
            }

            // 2. Filtre par nombre d'étoiles (min)
            if (searchDTO.getEtoilesMin() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                    root.get("etoiles"),
                    searchDTO.getEtoilesMin()
                ));
            }

            // 3. Filtre par prix (min/max) - optionnel
            if (searchDTO.getPrixMin() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                    chambresJoin.get("prixNuit"),
                    searchDTO.getPrixMin()
                ));
            }
            if (searchDTO.getPrixMax() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(
                    chambresJoin.get("prixNuit"),
                    searchDTO.getPrixMax()
                ));
            }

            // 4. Filtre par type de chambre (optionnel)
            if (searchDTO.getTypeChambre() != null && !searchDTO.getTypeChambre().isEmpty()) {
                predicates.add(criteriaBuilder.equal(
                    chambresJoin.get("typeChambre"),
                    searchDTO.getTypeChambre()
                ));
            }

            // 5. Seulement les hôtels actifs
            predicates.add(criteriaBuilder.isTrue(root.get("actif")));

            // 6. Seulement les chambres disponibles
            predicates.add(criteriaBuilder.isTrue(chambresJoin.get("disponible")));

            // Grouper par hôtel pour éviter les doublons
            query.groupBy(root.get("id"));
            
            // Ajouter la sélection du prix minimum
            query.multiselect(
                root,
                criteriaBuilder.min(chambresJoin.get("prixNuit"))
            );

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
    // Specification pour la recherche par géolocalisation (version simplifiée)
    public static Specification<Hotel> buildGeoSearchSpecification(Double latitude, Double longitude, Double radius) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (latitude != null && longitude != null && radius != null) {
                // Utiliser une approximation de distance (simplifiée)
                // Cette formule est approximative mais fonctionne pour des petites distances
                Expression<Double> latDiff = criteriaBuilder.diff(
                    criteriaBuilder.literal(latitude), 
                    root.get("latitude")
                );
                Expression<Double> lonDiff = criteriaBuilder.diff(
                    criteriaBuilder.literal(longitude), 
                    root.get("longitude")
                );
                
                // Distance approximative en degrés (1 degré ≈ 111 km)
                Expression<Double> distanceApprox = criteriaBuilder.sqrt(
                    criteriaBuilder.sum(
                        criteriaBuilder.prod(latDiff, latDiff),
                        criteriaBuilder.prod(lonDiff, lonDiff)
                    )
                );
                
                // Convertir en km (approximation)
                Expression<Double> distanceKm = criteriaBuilder.prod(distanceApprox, 111.0);
                predicates.add(criteriaBuilder.lessThanOrEqualTo(distanceKm, radius));
            }
            
            predicates.add(criteriaBuilder.isTrue(root.get("actif")));

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    // Spécification simplifiée pour la recherche rapide
    public static Specification<Hotel> buildQuickSearchSpecification(String query) {
        return (root, queryBuilder, criteriaBuilder) -> {
            if (query == null || query.isEmpty()) {
                return criteriaBuilder.conjunction();
            }

            String likePattern = "%" + query.toLowerCase() + "%";
            
            Predicate nomPredicate = criteriaBuilder.like(
                criteriaBuilder.lower(root.get("nom")), likePattern);
            
            Predicate villePredicate = criteriaBuilder.like(
                criteriaBuilder.lower(root.get("ville")), likePattern);
            
            Predicate descriptionPredicate = criteriaBuilder.like(
                criteriaBuilder.lower(root.get("description")), likePattern);
            
            return criteriaBuilder.or(
                nomPredicate,
                villePredicate,
                descriptionPredicate
            );
        };
    }
}
