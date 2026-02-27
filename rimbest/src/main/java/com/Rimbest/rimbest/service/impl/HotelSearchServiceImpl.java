package com.Rimbest.rimbest.service.impl;

import com.Rimbest.rimbest.model.Hotel;
import com.Rimbest.rimbest.model.dto.HotelSearchDTO;
import com.Rimbest.rimbest.model.dto.HotelCardDTO;
import com.Rimbest.rimbest.repository.HotelRepository;
import com.Rimbest.rimbest.service.HotelSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@Transactional(readOnly = true)
public class HotelSearchServiceImpl implements HotelSearchService {

    private static final Logger logger = LoggerFactory.getLogger(HotelSearchServiceImpl.class);

    @Autowired
    private HotelRepository hotelRepository;

    @Override
    public Page<HotelCardDTO> searchHotels(HotelSearchDTO searchDTO, Pageable pageable) {
        logger.info("=== DÉBUT searchHotels SIMPLIFIÉ ===");
        logger.info("Ville: {}, Page: {}, Size: {}", 
            searchDTO.getVille(), pageable.getPageNumber(), pageable.getPageSize());
        
        try {
            // 1. TEST: Vérifier que le repository fonctionne
            long totalHotels = hotelRepository.count();
            logger.info("Total hôtels en base: {}", totalHotels);
            
            // 2. Retourner une page TEST simple (sans erreur)
            List<HotelCardDTO> hotelsTest = createTestHotels(searchDTO.getVille());
            
            logger.info("=== FIN searchHotels - Retourne {} hôtels test ===", hotelsTest.size());
            
            return new PageImpl<>(hotelsTest, pageable, hotelsTest.size());
            
        } catch (Exception e) {
            logger.error("=== ERREUR CRITIQUE dans searchHotels ===", e);
            // Retourner une page vide en cas d'erreur
            return new PageImpl<>(new ArrayList<>(), pageable, 0);
        }
    }
    
    private List<HotelCardDTO> createTestHotels(String ville) {
        List<HotelCardDTO> hotels = new ArrayList<>();
        
        // Hôtel 1
        HotelCardDTO hotel1 = new HotelCardDTO();
        hotel1.setId(1L);
        hotel1.setNom("Hôtel Test " + (ville != null ? ville : "Marrakech"));
        hotel1.setVille(ville != null ? ville : "Marrakech");
        hotel1.setPays("Maroc");
        hotel1.setEtoiles(4);
        hotel1.setImageUrl("/images/hotel-default.jpg");
        hotel1.setNoteMoyenne(4.5);
        hotel1.setNombreAvis(120);
        hotel1.setPrixMinimum(250.0);
        hotel1.setPrixAffiche("À partir de 250€/nuit");
        hotel1.setDescriptionCourte("Hôtel de test pour le débogage - confort et qualité garantis");
        hotels.add(hotel1);
        
        // Hôtel 2
        HotelCardDTO hotel2 = new HotelCardDTO();
        hotel2.setId(2L);
        hotel2.setNom("Résidence " + (ville != null ? ville : "Casablanca"));
        hotel2.setVille(ville != null ? ville : "Casablanca");
        hotel2.setPays("Maroc");
        hotel2.setEtoiles(5);
        hotel2.setImageUrl("/images/hotel-default.jpg");
        hotel2.setNoteMoyenne(4.8);
        hotel2.setNombreAvis(89);
        hotel2.setPrixMinimum(350.0);
        hotel2.setPrixAffiche("À partir de 350€/nuit");
        hotel2.setDescriptionCourte("Résidence luxueuse avec toutes les commodités");
        hotels.add(hotel2);
        
        return hotels;
    }

    @Override
    public HotelCardDTO convertToHotelCardDTO(Hotel hotel) {
        logger.debug("Conversion hôtel ID: {}", hotel.getId());
        
        HotelCardDTO dto = new HotelCardDTO();
        dto.setId(hotel.getId());
        dto.setNom(hotel.getNom());
        dto.setVille(hotel.getVille());
        dto.setPays(hotel.getPays());
        dto.setEtoiles(hotel.getEtoiles());
        dto.setImageUrl("/images/hotel-default.jpg");
        dto.setNoteMoyenne(hotel.getNoteMoyenne());
        dto.setNombreAvis(hotel.getNombreAvis());
        dto.setPrixMinimum(100.0); // Valeur par défaut
        dto.setPrixAffiche("À partir de 100€/nuit");
        
        if (hotel.getDescription() != null && hotel.getDescription().length() > 100) {
            dto.setDescriptionCourte(hotel.getDescription().substring(0, 100) + "...");
        } else if (hotel.getDescription() != null) {
            dto.setDescriptionCourte(hotel.getDescription());
        } else {
            dto.setDescriptionCourte("Description non disponible");
        }
        
        return dto;
    }

    @Override
    public List<String> searchAutocomplete(String query) {
        logger.info("Autocomplete pour: {}", query);
        List<String> suggestions = new ArrayList<>();
        
        if (query != null && query.length() >= 2) {
            suggestions.add("Marrakech");
            suggestions.add("Casablanca");
            suggestions.add("Rabat");
            suggestions.add("Agadir");
            suggestions.add("Fès");
        }
        
        return suggestions;
    }

    // Autres méthodes avec implémentations vides
    @Override public List<HotelCardDTO> quickSearch(String query, int limit) { return new ArrayList<>(); }
    @Override public java.util.Map<String, Long> getSearchFilters(HotelSearchDTO criteria) { return java.util.Map.of(); }
    @Override public com.Rimbest.rimbest.model.dto.FilterOptionsDTO getFilterOptions(HotelSearchDTO searchDTO) { return new com.Rimbest.rimbest.model.dto.FilterOptionsDTO(); }
    @Override public com.Rimbest.rimbest.model.dto.SearchStatsDTO getSearchStats() { return new com.Rimbest.rimbest.model.dto.SearchStatsDTO(); }
    @Override public List<String> getVillesDisponibles() { 
        List<String> villes = new ArrayList<>();
        villes.add("Marrakech");
        villes.add("Casablanca");
        villes.add("Rabat");
        villes.add("Agadir");
        return villes;
    }
    @Override public List<HotelCardDTO> getHotelsByVille(String ville) { return new ArrayList<>(); }
    @Override public List<String> getPaysDisponibles() { 
        List<String> pays = new ArrayList<>();
        pays.add("Maroc");
        pays.add("France");
        pays.add("Espagne");
        return pays;
    }
    @Override public List<String> getTypesChambre() { return new ArrayList<>(); }
    @Override public com.Rimbest.rimbest.model.dto.PriceRangeDTO getPriceRange() { return new com.Rimbest.rimbest.model.dto.PriceRangeDTO(50.0, 500.0); }
    @Override public List<com.Rimbest.rimbest.model.dto.TopDestinationDTO> getTopDestinations(int limit) { return new ArrayList<>(); }
    @Override public List<HotelCardDTO> advancedSearch(HotelSearchDTO searchDTO, Pageable pageable) { return new ArrayList<>(); }
    @Override public List<HotelCardDTO> searchByLocation(Double latitude, Double longitude, Double radius, int limit) { return new ArrayList<>(); }
}