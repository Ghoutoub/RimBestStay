package com.Rimbest.rimbest.controller;

import com.Rimbest.rimbest.model.dto.HotelSearchDTO;
import com.Rimbest.rimbest.model.dto.HotelCardDTO;
import com.Rimbest.rimbest.service.HotelSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
@RequestMapping("/search")
public class HotelSearchController {

    private static final Logger logger = LoggerFactory.getLogger(HotelSearchController.class);
    
    @Autowired
    private HotelSearchService hotelSearchService;

    // ============ PAGE DE RECHERCHE - GET ============
    @GetMapping
    public String showSearchPage(Model model) {
        logger.info("Affichage page de recherche");
        
        HotelSearchDTO searchCriteria = new HotelSearchDTO();
        searchCriteria.setAdultes(2);
        searchCriteria.setEnfants(0);
        searchCriteria.setChambres(1);
        
        model.addAttribute("searchCriteria", searchCriteria);
        
        LocalDate aujourdhui = LocalDate.now();
        LocalDate demain = aujourdhui.plusDays(1);
        
        model.addAttribute("aujourdhui", aujourdhui);
        model.addAttribute("demain", demain);
        
        return "search/search";
    }

    // ============ TRAITEMENT DE LA RECHERCHE - GET ============
    @GetMapping("/results")
    public String processSearch(
            @RequestParam(value = "ville", required = false) String ville,
            @RequestParam(value = "dateArrivee", required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateArrivee,
            @RequestParam(value = "dateDepart", required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateDepart,
            @RequestParam(value = "adultes", defaultValue = "2") Integer adultes,
            @RequestParam(value = "enfants", defaultValue = "0") Integer enfants,
            @RequestParam(value = "chambres", defaultValue = "1") Integer chambres,
            @RequestParam(value = "prixMin", required = false) Double prixMin,
            @RequestParam(value = "prixMax", required = false) Double prixMax,
            @RequestParam(value = "etoilesMin", required = false) Integer etoilesMin,
            @RequestParam(value = "triPar", defaultValue = "recommandation") String triPar,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            Model model) {
        
        logger.info("Recherche - Ville: {}, Dates: {} à {}, Adultes: {}, Enfants: {}", 
                   ville, dateArrivee, dateDepart, adultes, enfants);
        
        try {
            // Créer l'objet de critères
            HotelSearchDTO searchCriteria = new HotelSearchDTO();
            searchCriteria.setVille(ville);
            searchCriteria.setDateArrivee(dateArrivee);
            searchCriteria.setDateDepart(dateDepart);
            searchCriteria.setAdultes(adultes != null ? adultes : 2);
            searchCriteria.setEnfants(enfants != null ? enfants : 0);
            searchCriteria.setChambres(chambres != null ? chambres : 1);
            searchCriteria.setPrixMin(prixMin);
            searchCriteria.setPrixMax(prixMax);
            searchCriteria.setEtoilesMin(etoilesMin);
            searchCriteria.setTriPar(triPar != null ? triPar : "recommandation");
            searchCriteria.setPage(page);
            searchCriteria.setTaille(size);
            
            // Validation des dates
            if (dateArrivee != null && dateDepart != null) {
                if (dateArrivee.isBefore(LocalDate.now())) {
                    model.addAttribute("error", "La date d'arrivée ne peut pas être dans le passé");
                    return showSearchPageWithError(model, "Date invalide");
                }
                if (dateArrivee.isAfter(dateDepart)) {
                    model.addAttribute("error", "La date d'arrivée doit être avant la date de départ");
                    return showSearchPageWithError(model, "Dates invalides");
                }
                if (dateArrivee.isEqual(dateDepart)) {
                    model.addAttribute("error", "La durée du séjour doit être d'au moins une nuit");
                    return showSearchPageWithError(model, "Durée invalide");
                }
            }
            
            // Pagination et tri
            Sort sort = getSortingStrategy(triPar);
            Pageable pageable = PageRequest.of(page, size, sort);
            
            logger.info("Recherche avec critères: {}", searchCriteria);
            
            // Recherche
            Page<HotelCardDTO> results = hotelSearchService.searchHotels(searchCriteria, pageable);
            
            logger.info("{} hôtels trouvés", results.getTotalElements());
            
            // Préparer le modèle
            model.addAttribute("searchCriteria", searchCriteria);
            model.addAttribute("hotels", results.getContent());
            model.addAttribute("currentPage", results.getNumber());
            model.addAttribute("totalPages", results.getTotalPages());
            model.addAttribute("totalHotels", results.getTotalElements());
            model.addAttribute("pageSize", size);
            
            return "search/results";
            
        } catch (Exception e) {
            logger.error("Erreur lors de la recherche: ", e);
            model.addAttribute("error", "Erreur lors de la recherche: " + e.getMessage());
            return showSearchPageWithError(model, e.getMessage());
        }
    }
    
    private String showSearchPageWithError(Model model, String errorMessage) {
        HotelSearchDTO searchCriteria = new HotelSearchDTO();
        searchCriteria.setAdultes(2);
        searchCriteria.setEnfants(0);
        searchCriteria.setChambres(1);
        
        model.addAttribute("searchCriteria", searchCriteria);
        model.addAttribute("error", errorMessage);
        
        LocalDate aujourdhui = LocalDate.now();
        LocalDate demain = aujourdhui.plusDays(1);
        
        model.addAttribute("aujourdhui", aujourdhui);
        model.addAttribute("demain", demain);
        
        return "search/search";
    }

    // ============ GESTION D'ERREURS ============
    @ExceptionHandler(Exception.class)
    public String handleException(Exception e, Model model) {
        logger.error("Erreur non gérée dans le contrôleur: ", e);
        
        HotelSearchDTO searchCriteria = new HotelSearchDTO();
        searchCriteria.setAdultes(2);
        searchCriteria.setEnfants(0);
        searchCriteria.setChambres(1);
        
        model.addAttribute("searchCriteria", searchCriteria);
        model.addAttribute("error", "Une erreur technique s'est produite. Veuillez réessayer.");
        
        LocalDate aujourdhui = LocalDate.now();
        LocalDate demain = aujourdhui.plusDays(1);
        
        model.addAttribute("aujourdhui", aujourdhui);
        model.addAttribute("demain", demain);
        
        return "search/search";
    }

    // ============ MÉTHODE DE TRI ============
    private Sort getSortingStrategy(String triPar) {
        if (triPar == null || triPar.isEmpty()) {
            return Sort.by("noteMoyenne").descending();
        }
        
        switch (triPar.toLowerCase()) {
            case "prix_croissant":
                return Sort.by("prixMinimumIndication").ascending();
            case "prix_decroissant":
                return Sort.by("prixMinimumIndication").descending();
            case "etoiles":
                return Sort.by("etoiles").descending();
            case "note":
                return Sort.by("noteMoyenne").descending();
            default: // recommandation
                return Sort.by("noteMoyenne").descending();
        }
    }

    // ============ AUTOCOMPLÉTION ============
    @GetMapping("/autocomplete")
    @ResponseBody
    public List<String> autocomplete(@RequestParam("q") String query) {
        try {
            logger.info("Autocomplete pour: {}", query);
            return hotelSearchService.searchAutocomplete(query);
        } catch (Exception e) {
            logger.error("Erreur autocomplete: ", e);
            return List.of();
        }
    }
}