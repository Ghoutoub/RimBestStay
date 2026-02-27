package com.Rimbest.rimbest.controller;

import com.Rimbest.rimbest.model.Chambre;
import com.Rimbest.rimbest.model.Hotel;
import com.Rimbest.rimbest.model.Reservation;
import com.Rimbest.rimbest.model.User;
import com.Rimbest.rimbest.service.ChambreService;
import com.Rimbest.rimbest.service.FileStorageService;
import com.Rimbest.rimbest.service.HotelService;
import com.Rimbest.rimbest.service.UserService;
import com.Rimbest.rimbest.service.ReservationService;
import com.Rimbest.rimbest.repository.ReservationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/chambre")
public class ChambreController {

    @Autowired
    private ChambreService chambreService;

    @Autowired
    private HotelService hotelService;

    @Autowired
    private UserService userService;
    @Autowired
    private ReservationService reservationService;
    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private FileStorageService fileStorageService;

    // ============ LISTE DES CHAMBRES D'UN HÔTEL ============

    @GetMapping("/list/{hotelId}")
    public String listChambres(
            @PathVariable("hotelId") Long hotelId,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "typeChambre", required = false) String typeChambre,
            @RequestParam(value = "minPrice", required = false) Double minPrice,
            @RequestParam(value = "maxPrice", required = false) Double maxPrice,
            @RequestParam(value = "disponible", required = false) Boolean disponible,
            Model model) {

        // Vérifier que l'hôtel existe
        Optional<Hotel> hotelOpt = hotelService.getHotelById(hotelId);
        if (hotelOpt.isEmpty()) {
            model.addAttribute("error", "Hôtel non trouvé");
            return "redirect:/hotel/list";
        }

        Hotel hotel = hotelOpt.get();
        model.addAttribute("hotel", hotel);

        // Vérifier les permissions
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String role = auth.getAuthorities().toString();
        User currentUser = userService.findByEmail(auth.getName()).orElse(null);

        boolean canManage = isOwner(hotel, currentUser);

        // Récupérer les chambres avec pagination
        Pageable pageable = PageRequest.of(page, size, Sort.by("numero").ascending());
        Page<Chambre> chambrePage = chambreService.searchChambresPage(
                hotelId, search, typeChambre, minPrice, maxPrice, disponible, pageable);

        // Statistiques
        long totalChambres = chambreService.countByHotel(hotel);
        long availableChambres = chambreService.countAvailableByHotel(hotel);
        long occupiedChambres = totalChambres - availableChambres;
        double occupancyRate = totalChambres > 0 ? (occupiedChambres * 100.0 / totalChambres) : 0;

        model.addAttribute("chambres", chambrePage.getContent());
        model.addAttribute("totalItems", chambrePage.getTotalElements());
        model.addAttribute("totalChambres", totalChambres);
        model.addAttribute("availableChambres", availableChambres);
        model.addAttribute("occupiedChambres", occupiedChambres);
        model.addAttribute("occupancyRate", Math.round(occupancyRate));

        // Paramètres de recherche
        model.addAttribute("search", search);
        model.addAttribute("typeChambre", typeChambre);
        model.addAttribute("minPrice", minPrice);
        model.addAttribute("maxPrice", maxPrice);
        model.addAttribute("disponible", disponible);

        // Pagination
        model.addAttribute("currentPage", page);
        model.addAttribute("pageSize", size);
        model.addAttribute("totalPages", chambrePage.getTotalPages());

        // Permissions
        model.addAttribute("canManage", canManage);
        model.addAttribute("isAdmin", role.contains("ROLE_ADMIN"));
        model.addAttribute("isPartenaire", role.contains("ROLE_PARTENAIRE"));
        model.addAttribute("isClient", role.contains("ROLE_CLIENT"));

        return "chambre/list";
    }

    // ============ DÉTAILS D'UNE CHAMBRE ============

    @GetMapping("/details/{id}")
    public String detailsChambre(
            @PathVariable("id") Long id,
            Model model) {

        Optional<Chambre> chambreOpt = chambreService.getChambreById(id);
        if (chambreOpt.isEmpty()) {
            return "redirect:/hotel/list?error=Chambre non trouvée";
        }

        Chambre chambre = chambreOpt.get();
        model.addAttribute("chambre", chambre);

        // Vérifier les permissions
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = userService.findByEmail(auth.getName()).orElse(null);

        boolean canManage = isOwner(chambre.getHotel(), currentUser);

        model.addAttribute("canManage", canManage);

        return "chambre/details";
    }
    // ============ AJOUT D'UNE CHAMBRE ============

    @PostMapping("/details/{id}/toggle-disponibilite")
    public String toggleChambreDisponibilite(@PathVariable("id") Long id,
            RedirectAttributes redirectAttributes) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            User currentUser = userService.findByEmail(auth.getName()).orElse(null);
            Chambre chambre = chambreService.toggleDisponibilite(id, currentUser);

            if (chambre.getDisponible()) {
                redirectAttributes.addFlashAttribute("success",
                        "Chambre marquée comme DISPONIBLE");
            } else {
                redirectAttributes.addFlashAttribute("warning",
                        "Chambre marquée comme INDISPONIBLE (maintenance)");
            }

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    "Erreur: " + e.getMessage());
        }

        return "redirect:/chambre/details/" + id;
    }

    @GetMapping("/add/{hotelId}")
    public String showAddForm(@PathVariable("hotelId") Long hotelId, Model model) {
        // Vérifier que l'hôtel existe
        Optional<Hotel> hotelOpt = hotelService.getHotelById(hotelId);
        if (hotelOpt.isEmpty()) {
            model.addAttribute("error", "Hôtel non trouvé");
            return "redirect:/hotel/list";
        }

        Hotel hotel = hotelOpt.get();

        // Vérifier les permissions
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = userService.findByEmail(auth.getName()).orElse(null);

        boolean canManage = isOwner(hotel, currentUser);

        if (!canManage) {
            model.addAttribute("error", "Vous n'avez pas la permission d'ajouter une chambre à cet hôtel");
            return "redirect:/hotel/details/" + hotelId;
        }

        Chambre chambre = new Chambre();
        chambre.setHotel(hotel);

        model.addAttribute("chambre", chambre);
        model.addAttribute("hotel", hotel);
        model.addAttribute("isEdit", false);

        // Types de chambre disponibles
        model.addAttribute("typesChambre", List.of(
                "SIMPLE", "DOUBLE", "TWIN", "SUITE", "DELUXE", "FAMILIALE", "EXECUTIVE", "PRESIDENTIELLE"));

        return "chambre/add";
    }

    @PostMapping("/add")
    public String addChambre(@Valid @ModelAttribute("chambre") Chambre chambre,
            BindingResult result,
            @RequestParam(value = "imageFiles", required = false) List<MultipartFile> imageFiles,
            @RequestParam(name = "hotelId", required = false) Long hotelIdParam,
            HttpServletRequest request,
            Model model,
            RedirectAttributes redirectAttributes) {

        // Essayer de récupérer l'ID de l'hôtel de plusieurs façons
        Long hotelId = hotelIdParam;

        if (hotelId == null) {
            String hotelIdStr = request.getParameter("hotelId");
            if (hotelIdStr != null) {
                try {
                    hotelId = Long.parseLong(hotelIdStr);
                } catch (NumberFormatException e) {
                }
            }
        }

        if (hotelId == null && chambre.getHotel() != null && chambre.getHotel().getId() != null) {
            hotelId = chambre.getHotel().getId();
        }

        if (hotelId == null) {
            redirectAttributes.addFlashAttribute("error", "ID de l'hôtel manquant. Veuillez réessayer.");
            return "redirect:/hotel/list";
        }

        // Récupérer l'hôtel
        Optional<Hotel> hotelOpt = hotelService.getHotelById(hotelId);
        if (hotelOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Hôtel non trouvé. ID: " + hotelId);
            return "redirect:/hotel/list";
        }

        Hotel hotel = hotelOpt.get();
        chambre.setHotel(hotel);

        // Validation manuelle
        if (chambre.getNumero() == null || chambre.getNumero().trim().isEmpty()) {
            result.rejectValue("numero", "error.chambre", "Le numéro de chambre est obligatoire");
        }
        if (chambre.getTypeChambre() == null || chambre.getTypeChambre().trim().isEmpty()) {
            result.rejectValue("typeChambre", "error.chambre", "Le type de chambre est obligatoire");
        }
        if (chambre.getPrixNuit() == null || chambre.getPrixNuit() <= 0) {
            result.rejectValue("prixNuit", "error.chambre", "Le prix doit être supérieur à 0");
        }

        if (result.hasErrors()) {
            model.addAttribute("isEdit", false);
            model.addAttribute("hotel", hotel);
            model.addAttribute("typesChambre", List.of(
                    "SIMPLE", "DOUBLE", "TWIN", "SUITE", "DELUXE", "FAMILIALE", "EXECUTIVE", "PRESIDENTIELLE"));
            return "chambre/add";
        }

        try {
            // Upload des images
            if (imageFiles != null && !imageFiles.isEmpty()) {
                List<String> imageUrls = fileStorageService.storeMultipleFiles(
                        imageFiles,
                        "chambres/" + hotel.getNom().replaceAll("[^a-zA-Z0-9]", "_") + "/" + chambre.getNumero());

                chambre.setImagesChambre(String.join(",", imageUrls));
            }
            // Vérifier l'unicité du numéro
            boolean exists = chambreService.existsByHotelAndNumero(hotel, chambre.getNumero());
            if (exists) {
                model.addAttribute("error", "Le numéro " + chambre.getNumero() + " existe déjà pour cet hôtel");
                model.addAttribute("isEdit", false);
                model.addAttribute("hotel", hotel);
                model.addAttribute("typesChambre", List.of(
                        "SIMPLE", "DOUBLE", "TWIN", "SUITE", "DELUXE", "FAMILIALE", "EXECUTIVE", "PRESIDENTIELLE"));
                return "chambre/add";
            }

            // Créer la chambre
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            User currentUser = userService.findByEmail(auth.getName()).orElse(null);
            Chambre savedChambre = chambreService.createChambre(chambre, currentUser);

            redirectAttributes.addFlashAttribute("success",
                    "Chambre " + savedChambre.getNumero() + " créée avec succès !");

            return "redirect:/chambre/details/" + savedChambre.getId();

        } catch (Exception e) {
            model.addAttribute("error", "Erreur technique: " + e.getMessage());
            model.addAttribute("isEdit", false);
            model.addAttribute("hotel", hotel);
            model.addAttribute("typesChambre", List.of(
                    "SIMPLE", "DOUBLE", "TWIN", "SUITE", "DELUXE", "FAMILIALE", "EXECUTIVE", "PRESIDENTIELLE"));
            return "chambre/add";
        }
    }

    // ============ MODIFICATION D'UNE CHAMBRE ============

    @GetMapping("/edit/{id}")
    public String showEditFormChambre(
            @PathVariable("id") Long id,
            Model model,
            RedirectAttributes redirectAttributes) {

        Optional<Chambre> chambreOpt = chambreService.getChambreById(id);
        if (chambreOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Chambre non trouvée");
            return "redirect:/chambre/list";
        }

        Chambre chambre = chambreOpt.get();
        model.addAttribute("chambre", chambre);
        model.addAttribute("typesChambre", Arrays.asList("SIMPLE", "DOUBLE", "TWIN", "SUITE", "DELUXE", "FAMILIALE",
                "EXECUTIVE", "PRESIDENTIELLE"));
        return "chambre/edit";
    }

    @PostMapping("/edit/{id}")
    public String updateChambre(
            @PathVariable("id") Long id,
            @Valid @ModelAttribute("chambre") Chambre chambreDetails,
            BindingResult result,
            @RequestParam(value = "imageFiles", required = false) List<MultipartFile> imageFiles,
            @RequestParam(value = "imagesToRemove", required = false) List<String> imagesToRemove,
            RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", "Formulaire invalide");
            return "redirect:/chambre/edit/" + id;
        }

        try {
            Optional<Chambre> existingChambreOpt = chambreService.getChambreById(id);
            if (existingChambreOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Chambre non trouvée");
                return "redirect:/chambre/edit/" + id;
            }

            Chambre existingChambre = existingChambreOpt.get();

            // Gérer la suppression des images
            String updatedImagesUrls = existingChambre.getImagesChambre();
            if (imagesToRemove != null && !imagesToRemove.isEmpty()) {
                if (updatedImagesUrls != null && !updatedImagesUrls.isEmpty()) {
                    List<String> currentImages = new java.util.ArrayList<>(Arrays.asList(updatedImagesUrls.split(",")));
                    for (String imageToRemove : imagesToRemove) {
                        currentImages.remove(imageToRemove.trim());
                        try {
                            fileStorageService.deleteFile(imageToRemove);
                        } catch (Exception e) {
                        }
                    }
                    updatedImagesUrls = currentImages.isEmpty() ? null : String.join(",", currentImages);
                }
            }

            // Upload des nouvelles images
            if (imageFiles != null && !imageFiles.isEmpty()) {
                List<String> imageUrls = fileStorageService.storeMultipleFiles(
                        imageFiles,
                        "chambres/" + existingChambre.getHotel().getNom().replaceAll("[^a-zA-Z0-9]", "_") + "/"
                                + chambreDetails.getNumero());

                StringBuilder urlsBuilder = new StringBuilder();
                if (updatedImagesUrls != null && !updatedImagesUrls.isEmpty()) {
                    urlsBuilder.append(updatedImagesUrls);
                }
                for (String url : imageUrls) {
                    if (urlsBuilder.length() > 0)
                        urlsBuilder.append(",");
                    urlsBuilder.append(url);
                }
                chambreDetails.setImagesChambre(urlsBuilder.toString());
            } else {
                chambreDetails.setImagesChambre(updatedImagesUrls);
            }

            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            User currentUser = userService.findByEmail(auth.getName()).orElse(null);
            Chambre updated = chambreService.updateChambre(id, chambreDetails, currentUser);
            redirectAttributes.addFlashAttribute("success", "Chambre " + updated.getNumero() + " modifiée !");
            return "redirect:/chambre/details/" + id;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur: " + e.getMessage());
            return "redirect:/chambre/edit/" + id;
        }
    }

    // ============ SUPPRESSION D'UNE CHAMBRE ============

    @PostMapping("/delete/{id}")
    public String deleteChambre(@PathVariable("id") Long id,
            RedirectAttributes redirectAttributes) {

        try {
            Optional<Chambre> chambreOpt = chambreService.getChambreById(id);
            if (chambreOpt.isPresent()) {
                Chambre chambre = chambreOpt.get();
                Long hotelId = chambre.getHotel().getId();

                Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                User currentUser = userService.findByEmail(auth.getName()).orElse(null);

                chambreService.deleteChambre(id, currentUser);
                redirectAttributes.addFlashAttribute("success",
                        "Chambre " + chambre.getNumero() + " supprimée avec succès!");
                return "redirect:/chambre/list/" + hotelId;
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    "Erreur lors de la suppression: " + e.getMessage());
        }

        return "redirect:/hotel/list";
    }

    // ============ TOGGLE DISPONIBILITÉ ============

    @PostMapping("/toggle-disponibilite/{id}")
    public String toggleDisponibilite(@PathVariable("id") Long id,
            RedirectAttributes redirectAttributes) {

        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            User currentUser = userService.findByEmail(auth.getName()).orElse(null);

            Chambre chambre = chambreService.toggleDisponibilite(id, currentUser);
            String status = chambre.getDisponible() ? "disponible" : "indisponible";

            redirectAttributes.addFlashAttribute("success",
                    "Chambre " + chambre.getNumero() + " marquée comme " + status);

            if (!chambre.getDisponible()) {
                List<Reservation> reservationsFutures = reservationRepository
                        .findByChambreAndDateArriveeAfter(chambre, LocalDate.now());

                if (!reservationsFutures.isEmpty()) {
                    redirectAttributes.addFlashAttribute("warning",
                            "Attention: " + reservationsFutures.size() +
                                    " réservation(s) future(s) affectée(s)");
                }
            }

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    "Erreur: " + e.getMessage());
        }

        return "redirect:/chambre/details/" + id;
    }

    // ============ REDIRECTION POUR HOTEL/{ID}/CHAMBRES ============

    @GetMapping("/hotel/{hotelId}/chambres")
    public String redirectToList(@PathVariable("hotelId") Long hotelId) {
        return "redirect:/chambre/list/" + hotelId;
    }

    @GetMapping("/hotel/{hotelId}/chambres/add")
    public String redirectToAddFromHotel(@PathVariable("hotelId") Long hotelId) {
        return "redirect:/chambre/add/" + hotelId;
    }

    // ============ API POUR STATISTIQUES ============

    @GetMapping("/api/statistics/{hotelId}")
    @ResponseBody
    public Object getStatistics(@PathVariable("hotelId") Long hotelId) {
        Optional<Hotel> hotelOpt = hotelService.getHotelById(hotelId);

        if (hotelOpt.isEmpty()) {
            return new Object() {
                public final String error = "Hôtel non trouvé";
            };
        }

        Hotel hotel = hotelOpt.get();
        long total = chambreService.countByHotel(hotel);
        long available = chambreService.countAvailableByHotel(hotel);
        long occupied = total - available;
        double occupancy = total > 0 ? (occupied * 100.0 / total) : 0;

        return new Object() {
            public final long totalChambres = total;
            public final long availableChambres = available;
            public final long occupiedChambres = occupied;
            public final double occupancyRate = Math.round(occupancy * 100.0) / 100.0;
            public final String hotelName = hotel.getNom();
        };
    }

    private boolean isOwner(Hotel hotel, User user) {
        if (hotel == null || user == null)
            return false;
        return (hotel.getPartenaire() != null && hotel.getPartenaire().getId().equals(user.getId()));
    }
}