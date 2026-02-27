package com.Rimbest.rimbest.service;

import com.Rimbest.rimbest.model.Hotel;
import com.Rimbest.rimbest.model.dto.HotelSearchDTO;
import com.Rimbest.rimbest.model.dto.HotelCardDTO;
import com.Rimbest.rimbest.model.dto.FilterOptionsDTO;
import com.Rimbest.rimbest.model.dto.PriceRangeDTO;
import com.Rimbest.rimbest.model.dto.SearchStatsDTO;
import com.Rimbest.rimbest.model.dto.TopDestinationDTO;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

public interface HotelSearchService {
    
    // Recherche principale avec pagination
    Page<HotelCardDTO> searchHotels(HotelSearchDTO searchDTO, Pageable pageable);
    
    // Recherche rapide
    List<HotelCardDTO> quickSearch(String query, int limit);
    
    // Suggestions d'autocomplétion
    List<String> searchAutocomplete(String query);
    
    // Filtres de recherche
    Map<String, Long> getSearchFilters(HotelSearchDTO criteria);
    
    // Options de filtres
    FilterOptionsDTO getFilterOptions(HotelSearchDTO searchDTO);
    
    // Statistiques de recherche
    SearchStatsDTO getSearchStats();
    
    // Villes disponibles
    List<String> getVillesDisponibles();
    
    // Hôtels par ville
    List<HotelCardDTO> getHotelsByVille(String ville);
    
    // Pays disponibles
    List<String> getPaysDisponibles();
    
    // Types de chambre disponibles
    List<String> getTypesChambre();
    
    // Gamme de prix
    PriceRangeDTO getPriceRange();
    
    // Destinations populaires
    List<TopDestinationDTO> getTopDestinations(int limit);
    
    // Conversion Hotel -> HotelCardDTO
    HotelCardDTO convertToHotelCardDTO(Hotel hotel);
    
    // Recherche avancée avec Pageable
    List<HotelCardDTO> advancedSearch(HotelSearchDTO searchDTO, Pageable pageable);
    
    // Recherche par localisation
    List<HotelCardDTO> searchByLocation(Double latitude, Double longitude, Double radius, int limit);
}