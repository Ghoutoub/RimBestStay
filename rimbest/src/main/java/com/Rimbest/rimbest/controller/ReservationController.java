package com.Rimbest.rimbest.controller;

import com.Rimbest.rimbest.model.dto.ReservationDTO;
import com.Rimbest.rimbest.model.dto.ReservationRequestDTO;
import com.Rimbest.rimbest.model.Chambre;
import com.Rimbest.rimbest.model.Hotel;
import com.Rimbest.rimbest.model.Reservation;
import com.Rimbest.rimbest.model.StatutReservation;
import com.Rimbest.rimbest.model.User;
import com.Rimbest.rimbest.model.Partenaire;
import com.Rimbest.rimbest.service.ReservationService;
import com.Rimbest.rimbest.service.UserService;
import com.Rimbest.rimbest.service.ChambreService;
import com.Rimbest.rimbest.service.HotelService;
import com.Rimbest.rimbest.repository.ChambreRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ArrayList;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;
    private final UserService userService;
    private final ChambreService chambreService;
    private final ChambreRepository chambreRepository;
    private final HotelService hotelService;

    // ============ VUE CLIENT ============

    @GetMapping("/client")
    public String clientReservations(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "50") int size,
            @RequestParam(value = "statut", required = false) String statut,
            @RequestParam(value = "dateFrom", required = false) LocalDate dateFrom,
            @RequestParam(value = "dateTo", required = false) LocalDate dateTo,
            Model model) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = userService.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        // Récupérer toutes les réservations du client
        List<Reservation> allReservations = reservationService.getReservationsByClient(currentUser);

        System.out.println("=== DEBUG RESERVATIONS ===");
        System.out.println("Total réservations trouvées: " + allReservations.size());
        System.out.println("Utilisateur: " + currentUser.getEmail());

        if (!allReservations.isEmpty()) {
            System.out.println("Première réservation ID: " + allReservations.get(0).getId());
            System.out.println("Première réservation Reference: " + allReservations.get(0).getReference());
            if (allReservations.size() > 1) {
                System.out.println("Deuxième réservation ID: " + allReservations.get(1).getId());
                System.out.println("Deuxième réservation Reference: " + allReservations.get(1).getReference());
            }
        }

        // Filtrer par statut si spécifié
        if (statut != null && !statut.isEmpty()) {
            allReservations = allReservations.stream()
                    .filter(r -> r.getStatut().toString().equals(statut))
                    .collect(Collectors.toList());
        }

        // Filtrer par dates si spécifiées
        if (dateFrom != null) {
            allReservations = allReservations.stream()
                    .filter(r -> !r.getDateArrivee().isBefore(dateFrom))
                    .collect(Collectors.toList());
        }

        if (dateTo != null) {
            allReservations = allReservations.stream()
                    .filter(r -> !r.getDateArrivee().isAfter(dateTo))
                    .collect(Collectors.toList());
        }

        // Pagination manuelle
        int totalItems = allReservations.size();
        int totalPages = (int) Math.ceil((double) totalItems / size);
        int start = page * size;
        int end = Math.min(start + size, totalItems);

        List<Reservation> pageReservations = allReservations.subList(start, end);
        List<ReservationDTO> reservations = reservationService.convertToDTOList(pageReservations);

        // Calculer les statistiques
        long confirmedCount = allReservations.stream()
                .filter(r -> r.getStatut() == StatutReservation.CONFIRMEE)
                .count();

        long pendingCount = allReservations.stream()
                .filter(r -> r.getStatut() == StatutReservation.EN_ATTENTE)
                .count();

        long upcomingCount = allReservations.stream()
                .filter(r -> r.getStatut() == StatutReservation.CONFIRMEE &&
                        r.getDateArrivee().isAfter(LocalDate.now()))
                .count();

        // Ajouter les attributs au modèle
        model.addAttribute("reservations", reservations);
        model.addAttribute("totalItems", totalItems);
        model.addAttribute("currentPage", page);
        model.addAttribute("pageSize", size);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("statut", statut);
        model.addAttribute("dateFrom", dateFrom);
        model.addAttribute("dateTo", dateTo);
        model.addAttribute("confirmedCount", confirmedCount);
        model.addAttribute("pendingCount", pendingCount);
        model.addAttribute("upcomingCount", upcomingCount);

        return "client/reservations/liste";
    }

    @GetMapping("/client/details/{id}")
    public String clientReservationDetails(@PathVariable Long id, Model model) {
        Reservation reservation = reservationService.getReservationById(id);

        // Vérifier que l'utilisateur a le droit de voir cette réservation
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = userService.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        if (!reservation.getClient().getId().equals(currentUser.getId())) {
            return "redirect:/reservations/client?error=unauthorized";
        }

        ReservationDTO dto = reservationService.convertToDTO(reservation);
        model.addAttribute("reservation", dto);

        // Ajouter les objets chambre et hôtel nécessaires pour le template
        if (reservation.getChambre() != null) {
            model.addAttribute("chambre", reservation.getChambre());
            if (reservation.getChambre().getHotel() != null) {
                model.addAttribute("hotel", reservation.getChambre().getHotel());
            }
        }

        // Calculer le nombre de nuits pour l'affichage
        long nombreNuits = ChronoUnit.DAYS.between(
                reservation.getDateArrivee(),
                reservation.getDateDepart());
        model.addAttribute("nombreNuits", nombreNuits);

        // Calculer sous-total et taxes pour l'affichage
        double prixNuit = reservation.getChambre() != null ? reservation.getChambre().getPrixNuit() : 0;
        double sousTotal = prixNuit * nombreNuits;
        double taxes = reservation.getChambre() != null && reservation.getChambre().getTaxeSejour() != null
                ? reservation.getChambre().getTaxeSejour()
                : 0;

        model.addAttribute("sousTotal", sousTotal);
        model.addAttribute("taxes", taxes);

        // Calculer des détails supplémentaires pour l'affichage
        if (reservation.getChambre() != null) {
            // Prix par nuit (standard)
            model.addAttribute("prixNuit", reservation.getChambre().getPrixNuit());

            // Prix weekend si applicable
            if (reservation.getChambre().getPrixWeekend() != null) {
                model.addAttribute("prixWeekend", reservation.getChambre().getPrixWeekend());
            }

            // Dépôt de garantie
            if (reservation.getChambre().getDepotGarantie() != null) {
                model.addAttribute("depotGarantie", reservation.getChambre().getDepotGarantie());
            }

            // Taxe de séjour
            if (reservation.getChambre().getTaxeSejour() != null) {
                model.addAttribute("taxeSejour", reservation.getChambre().getTaxeSejour());
            }

            // Calcul du total taxes
            double totalTaxes = (reservation.getChambre().getTaxeSejour() != null
                    ? reservation.getChambre().getTaxeSejour()
                    : 20.0) *
                    reservation.getNombrePersonnes() * nombreNuits;
            model.addAttribute("totalTaxes", totalTaxes);

            // Calcul du sous-total sans taxes
            double sousTotalSansTaxes = reservation.getChambre().getPrixNuit() * nombreNuits;
            model.addAttribute("sousTotalSansTaxes", sousTotalSansTaxes);

            // Calcul des nuits weekend (simplifié)
            int weekendNuits = 0; // Pourrait être calculé en fonction des dates
            model.addAttribute("weekendNuits", weekendNuits);
        }

        // Autres informations de la réservation
        if (reservation.getReduction() != null) {
            model.addAttribute("reduction", reservation.getReduction());
        }

        if (reservation.getDepotGarantie() != null) {
            model.addAttribute("reservationDepotGarantie", reservation.getDepotGarantie());
        }

        return "client/reservations/details";
    }

    @GetMapping("/client/nouvelle/{chambreId}")
    public String nouvelleReservation(@PathVariable Long chambreId, Model model) {
        // Récupérer la chambre
        Chambre chambre = chambreService.getChambreById(chambreId)
                .orElseThrow(() -> new RuntimeException("Chambre non trouvée"));

        // Récupérer l'hôtel
        Hotel hotel = chambre.getHotel();

        // Vérifier la disponibilité de base
        if (!chambre.getDisponible()) {
            model.addAttribute("error", "Cette chambre n'est pas disponible pour le moment");
            return "redirect:/chambre/details/" + chambreId;
        }

        // Préparer les données pour le formulaire
        ReservationRequestDTO reservationRequest = new ReservationRequestDTO();
        reservationRequest.setChambreId(chambreId);
        reservationRequest.setNbPersonnes(1); // Valeur par défaut

        model.addAttribute("reservationRequest", reservationRequest);
        model.addAttribute("chambre", chambre);
        model.addAttribute("hotel", hotel);
        model.addAttribute("chambreId", chambreId);
        model.addAttribute("aujourdhui", LocalDate.now());
        model.addAttribute("demain", LocalDate.now().plusDays(1));

        return "client/reservations/nouvelle";
    }

    @GetMapping("/client/confirmation/{id}")
    public String confirmationReservation(@PathVariable Long id, Model model) {
        Reservation reservation = reservationService.getReservationById(id);
        ReservationDTO dto = reservationService.convertToDTO(reservation);

        // Calculer le nombre de nuits
        long nombreNuits = ChronoUnit.DAYS.between(
                reservation.getDateArrivee(),
                reservation.getDateDepart());

        model.addAttribute("reservation", dto);
        model.addAttribute("hotel", reservation.getChambre().getHotel());
        model.addAttribute("chambre", reservation.getChambre());
        model.addAttribute("nombreNuits", nombreNuits);
        model.addAttribute("reservationDate", LocalDateTime.now());

        return "client/reservations/confirmation";
    }

    @PostMapping("/client/create")
    public String createReservation(@Valid @ModelAttribute ReservationRequestDTO requestDTO,
            BindingResult result,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            model.addAttribute("chambreId", requestDTO.getChambreId());
            model.addAttribute("aujourdhui", LocalDate.now());
            return "client/reservations/nouvelle";
        }

        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            User currentUser = userService.findByEmail(auth.getName())
                    .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

            Reservation reservation = reservationService.createReservation(requestDTO, currentUser);

            redirectAttributes.addFlashAttribute("success",
                    "Réservation créée avec succès ! Référence: " + reservation.getReference());
            return "redirect:/reservations/client/details/" + reservation.getId();

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur: " + e.getMessage());
            return "redirect:/chambre/details/" + requestDTO.getChambreId();
        }
    }

    @PostMapping("/client/annuler/{id}")
    public String annulerReservation(@PathVariable Long id,
            @RequestParam String raison,
            RedirectAttributes redirectAttributes) {

        try {
            reservationService.refuseReservation(id, raison);
            redirectAttributes.addFlashAttribute("success",
                    "Réservation annulée avec succès");
            return "redirect:/reservations/client";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur: " + e.getMessage());
            return "redirect:/reservations/client";
        }
    }

    // ============ VUE PARTENAIRE ============

    @GetMapping("/partenaire")
    public String partenaireReservations(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "hotelId", required = false) Long hotelId,
            @RequestParam(value = "statut", required = false) String statut,
            Model model) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = userService.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        List<Reservation> reservations = reservationService.getReservationsByPartenaire(currentUser);

        // Filtrer par hôtel si spécifié
        if (hotelId != null) {
            reservations = reservations.stream()
                    .filter(r -> r.getChambre().getHotel().getId().equals(hotelId))
                    .toList();
        }

        // Filtrer par statut si spécifié
        if (statut != null) {
            reservations = reservations.stream()
                    .filter(r -> r.getStatut().toString().equals(statut))
                    .toList();
        }

        // Pagination manuelle
        int start = page * size;
        int end = Math.min(start + size, reservations.size());
        List<Reservation> pageReservations = reservations.subList(start, end);

        List<ReservationDTO> reservationsDTO = reservationService.convertToDTOList(pageReservations);

        // Calculer les statistiques
        long confirmedCount = reservations.stream()
                .filter(r -> r.getStatut() == StatutReservation.CONFIRMEE)
                .count();

        long pendingCount = reservations.stream()
                .filter(r -> r.getStatut() == StatutReservation.EN_ATTENTE)
                .count();

        long cancelledCount = reservations.stream()
                .filter(r -> r.getStatut() == StatutReservation.ANNULEE)
                .count();

        long totalItems = reservations.size();

        model.addAttribute("reservations", reservationsDTO);
        model.addAttribute("totalItems", reservations.size());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", (int) Math.ceil((double) reservations.size() / size));
        model.addAttribute("hotelId", hotelId);
        model.addAttribute("statut", statut);

        // Calculer les statistiques
        model.addAttribute("confirmedCount", confirmedCount);
        model.addAttribute("pendingCount", pendingCount);
        model.addAttribute("cancelledCount", cancelledCount);

        // Récupérer les hôtels du partenaire
        if (currentUser instanceof Partenaire) {
            List<Hotel> myHotels = hotelService.findHotelsByPartenaire(currentUser);
            model.addAttribute("myHotels", myHotels);
        } else {
            model.addAttribute("myHotels", new ArrayList<>());
        }

        return "partenaire/reservations/liste";
    }

    // GET pour afficher les détails d'une réservation (partenaire)
    @GetMapping("/partenaire/details/{id}")
    public String partenaireReservationDetails(@PathVariable Long id, Model model) {
        try {
            Reservation reservation = reservationService.getReservationById(id);

            // Vérifier que le partenaire a le droit de voir cette réservation
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            User partenaire = userService.findByEmail(auth.getName())
                    .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

            // Vérifier que la réservation appartient à un hôtel du partenaire
            if (!reservation.getChambre().getHotel().getPartenaire().getId().equals(partenaire.getId())) {
                model.addAttribute("error", "Vous n'avez pas accès à cette réservation");
                return "redirect:/reservations/partenaire";
            }

            ReservationDTO dto = reservationService.convertToDTO(reservation);

            // Ajouter les objets chambre et hôtel nécessaires pour le template
            if (reservation.getChambre() != null) {
                model.addAttribute("chambre", reservation.getChambre());
                if (reservation.getChambre().getHotel() != null) {
                    model.addAttribute("hotel", reservation.getChambre().getHotel());
                }
            }

            // Ajouter les informations client
            if (reservation.getClient() != null) {
                model.addAttribute("client", reservation.getClient());
            }

            // Calculer le nombre de nuits
            long nombreNuits = ChronoUnit.DAYS.between(
                    reservation.getDateArrivee(),
                    reservation.getDateDepart());

            // Calculer sous-total et taxes pour l'affichage
            double prixNuit = reservation.getChambre() != null ? reservation.getChambre().getPrixNuit() : 0;
            double sousTotal = prixNuit * nombreNuits;
            double taxes = reservation.getChambre() != null && reservation.getChambre().getTaxeSejour() != null
                    ? reservation.getChambre().getTaxeSejour()
                    : 0;

            model.addAttribute("reservation", dto);
            model.addAttribute("nombreNuits", nombreNuits);
            model.addAttribute("sousTotal", sousTotal);
            model.addAttribute("taxes", taxes);

            return "partenaire/reservations/details";

        } catch (Exception e) {
            model.addAttribute("error", "Erreur: " + e.getMessage());
            return "redirect:/reservations/partenaire";
        }
    }

    // POST pour confirmer une réservation (partenaire)
    @PostMapping("/partenaire/confirmer/{id}")
    public String confirmerReservationPartenaire(@PathVariable Long id,
            RedirectAttributes redirectAttributes) {
        try {
            System.out.println("DEBUG: Tentative de confirmation de la réservation #" + id);
            // Vérifier que le partenaire a le droit
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            User partenaire = userService.findByEmail(auth.getName())
                    .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

            Reservation reservation = reservationService.getReservationById(id);
            System.out.println("DEBUG: Réservation trouvée - Status actuel: " + reservation.getStatut());

            // Vérifier que la réservation appartient à un hôtel du partenaire
            if (!reservation.getChambre().getHotel().getPartenaire().getId().equals(partenaire.getId())) {
                redirectAttributes.addFlashAttribute("error",
                        "Vous n'êtes pas autorisé à confirmer cette réservation");
                return "redirect:/reservations/partenaire";
            }

            // Confirmer la réservation
            reservation = reservationService.confirmReservation(id);
            System.out.println("DEBUG: Réservation confirmée - Nouveau status: " + reservation.getStatut());

            redirectAttributes.addFlashAttribute("success",
                    "Réservation #" + reservation.getReference() + " confirmée avec succès");

        } catch (Exception e) {
            System.out.println("DEBUG: Erreur lors de la confirmation: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error",
                    "Erreur: " + e.getMessage());
        }

        return "redirect:/reservations/partenaire";
    }

    // POST pour refuser une réservation (partenaire)
    @PostMapping("/partenaire/refuser/{id}")
    public String refuserReservationPartenaire(@PathVariable Long id,
            @RequestParam(value = "raison", defaultValue = "Refusé par le partenaire") String raison,
            RedirectAttributes redirectAttributes) {
        try {
            System.out.println("DEBUG: Tentative de refus de la réservation #" + id);
            // Vérifier que le partenaire a le droit
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            User partenaire = userService.findByEmail(auth.getName())
                    .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

            Reservation reservation = reservationService.getReservationById(id);
            System.out.println("DEBUG: Réservation trouvée - Status actuel: " + reservation.getStatut());

            // Vérifier que la réservation appartient à un hôtel du partenaire
            if (!reservation.getChambre().getHotel().getPartenaire().getId().equals(partenaire.getId())) {
                redirectAttributes.addFlashAttribute("error",
                        "Vous n'êtes pas autorisé à refuser cette réservation");
                return "redirect:/reservations/partenaire";
            }

            // Refuser la réservation
            reservation = reservationService.refuseReservation(id, raison);
            System.out.println("DEBUG: Réservation refusée - Nouveau status: " + reservation.getStatut());

            redirectAttributes.addFlashAttribute("success",
                    "Réservation #" + reservation.getReference() + " refusée avec succès");

        } catch (Exception e) {
            System.out.println("DEBUG: Erreur lors du refus: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error",
                    "Erreur: " + e.getMessage());
        }

        return "redirect:/reservations/partenaire";
    }

    // POST pour annuler une réservation (partenaire)
    @PostMapping("/partenaire/annuler/{id}")
    public String annulerReservationPartenaire(@PathVariable Long id,
            @RequestParam(value = "raison", defaultValue = "Annulé par le partenaire") String raison,
            RedirectAttributes redirectAttributes) {
        try {
            // Vérifier que le partenaire a le droit
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            User partenaire = userService.findByEmail(auth.getName())
                    .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

            Reservation reservation = reservationService.getReservationById(id);

            // Vérifier que la réservation appartient à un hôtel du partenaire
            if (!reservation.getChambre().getHotel().getPartenaire().getId().equals(partenaire.getId())) {
                redirectAttributes.addFlashAttribute("error",
                        "Vous n'êtes pas autorisé à annuler cette réservation");
                return "redirect:/reservations/partenaire";
            }

            // Annuler la réservation
            reservation = reservationService.refuseReservation(id, raison);

            redirectAttributes.addFlashAttribute("success",
                    "Réservation #" + reservation.getReference() + " annulée avec succès");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    "Erreur: " + e.getMessage());
        }

        return "redirect:/reservations/partenaire";
    }

    // ============ VUE ADMIN ============

    @GetMapping("/admin")
    public String adminReservations(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "15") int size,
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "statut", required = false) String statut,
            @RequestParam(value = "dateFrom", required = false) LocalDate dateFrom,
            @RequestParam(value = "dateTo", required = false) LocalDate dateTo,
            Model model) {

        // Récupérer toutes les réservations
        List<Reservation> allReservations = reservationService.getReservationsPage(PageRequest.of(0, 10000))
                .getContent();

        // Filtrer par statut si spécifié
        if (statut != null && !statut.isEmpty()) {
            allReservations = allReservations.stream()
                    .filter(r -> r.getStatut().toString().equals(statut))
                    .collect(Collectors.toList());
        }

        // Filtrer par dates si spécifiées
        if (dateFrom != null) {
            allReservations = allReservations.stream()
                    .filter(r -> !r.getDateArrivee().isBefore(dateFrom))
                    .collect(Collectors.toList());
        }

        if (dateTo != null) {
            allReservations = allReservations.stream()
                    .filter(r -> !r.getDateArrivee().isAfter(dateTo))
                    .collect(Collectors.toList());
        }

        // Filtrer par recherche si spécifiée
        if (search != null && !search.trim().isEmpty()) {
            allReservations = allReservations.stream()
                    .filter(r -> r.getReference().toLowerCase().contains(search.toLowerCase()) ||
                            (r.getClient() != null
                                    && r.getClient().getNom().toLowerCase().contains(search.toLowerCase()))
                            ||
                            (r.getChambre() != null
                                    && r.getChambre().getNumero().toLowerCase().contains(search.toLowerCase())))
                    .collect(Collectors.toList());
        }

        // Pagination manuelle
        int totalItems = allReservations.size();
        int totalPages = (int) Math.ceil((double) totalItems / size);
        int start = page * size;
        int end = Math.min(start + size, totalItems);

        List<Reservation> pageReservations = allReservations.subList(start, end);
        List<ReservationDTO> reservations = reservationService.convertToDTOList(pageReservations);

        // Calculer les statistiques
        long confirmedCount = allReservations.stream()
                .filter(r -> r.getStatut() == StatutReservation.CONFIRMEE)
                .count();

        long pendingCount = allReservations.stream()
                .filter(r -> r.getStatut() == StatutReservation.EN_ATTENTE)
                .count();

        long cancelledCount = allReservations.stream()
                .filter(r -> r.getStatut() == StatutReservation.ANNULEE)
                .count();

        long todayCount = allReservations.stream()
                .filter(r -> r.getDateArrivee().equals(LocalDate.now()))
                .count();

        // Ajouter les attributs au modèle
        model.addAttribute("reservations", reservations);
        model.addAttribute("totalItems", totalItems);
        model.addAttribute("currentPage", page);
        model.addAttribute("pageSize", size);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("search", search);
        model.addAttribute("statut", statut);
        model.addAttribute("dateFrom", dateFrom);
        model.addAttribute("dateTo", dateTo);
        model.addAttribute("confirmedCount", confirmedCount);
        model.addAttribute("pendingCount", pendingCount);
        model.addAttribute("cancelledCount", cancelledCount);
        model.addAttribute("todayCount", todayCount);

        return "admin/reservations/liste";
    }

    @GetMapping("/admin/dashboard")
    public String adminReservationsDashboard(Model model) {
        // Statistiques
        long totalReservations = reservationService.countTotalReservations();
        long reservationsToday = reservationService.countReservationsToday();
        long pendingReservations = reservationService.getReservationsPage(PageRequest.of(0, 10000)).getContent()
                .stream()
                .filter(r -> r.getStatut() == StatutReservation.EN_ATTENTE)
                .count();
        BigDecimal revenueMonth = reservationService.getRevenueForCurrentMonth();

        model.addAttribute("totalReservations", totalReservations);
        model.addAttribute("reservationsToday", reservationsToday);
        model.addAttribute("pendingReservations", pendingReservations);
        model.addAttribute("revenueMonth", revenueMonth);

        return "admin/reservations/dashboard";
    }

    @GetMapping("/admin/details/{id}")
    public String adminReservationDetails(@PathVariable Long id, Model model) {
        Reservation reservation = reservationService.getReservationById(id);
        ReservationDTO dto = reservationService.convertToDTO(reservation);

        // Ajouter les objets chambre et hôtel nécessaires pour le template
        if (reservation.getChambre() != null) {
            model.addAttribute("chambre", reservation.getChambre());
            if (reservation.getChambre().getHotel() != null) {
                model.addAttribute("hotel", reservation.getChambre().getHotel());
            }
        }

        // Ajouter les informations client
        if (reservation.getClient() != null) {
            model.addAttribute("client", reservation.getClient());
        }

        // Calculer le nombre de nuits
        long nombreNuits = ChronoUnit.DAYS.between(
                reservation.getDateArrivee(),
                reservation.getDateDepart());

        // Calculer sous-total et taxes pour l'affichage
        double prixNuit = reservation.getChambre() != null ? reservation.getChambre().getPrixNuit() : 0;
        double sousTotal = prixNuit * nombreNuits;
        double taxes = reservation.getChambre() != null && reservation.getChambre().getTaxeSejour() != null
                ? reservation.getChambre().getTaxeSejour()
                : 0;

        model.addAttribute("reservation", dto);
        model.addAttribute("nombreNuits", nombreNuits);
        model.addAttribute("sousTotal", sousTotal);
        model.addAttribute("taxes", taxes);

        return "admin/reservations/details";
    }

    // ============ MÉTHODES UTILITAIRES ============

    // ============ ADMIN ACTIONS ============

    @PostMapping("/admin/confirmer/{id}")
    public String confirmerReservationAdmin(@PathVariable Long id,
            RedirectAttributes redirectAttributes) {
        try {
            Reservation reservation = reservationService.getReservationById(id);

            // Confirmer la réservation
            reservation = reservationService.confirmReservation(id);

            redirectAttributes.addFlashAttribute("success",
                    "Réservation #" + reservation.getReference() + " confirmée avec succès");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    "Erreur: " + e.getMessage());
        }

        return "redirect:/reservations/admin";
    }

    @PostMapping("/admin/refuser/{id}")
    public String refuserReservationAdmin(@PathVariable Long id,
            @RequestParam(value = "raison", required = false) String raison,
            RedirectAttributes redirectAttributes) {
        try {
            String raisonComplete = (raison != null && !raison.isEmpty()) ? raison : "Refusée par l'administrateur";

            Reservation reservation = reservationService.refuseReservation(id, raisonComplete);

            redirectAttributes.addFlashAttribute("success",
                    "Réservation #" + reservation.getReference() + " refusée par l'admin");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    "Erreur: " + e.getMessage());
        }

        return "redirect:/reservations/admin";
    }

    @PostMapping("/admin/annuler/{id}")
    public String annulerReservationAdmin(@PathVariable Long id,
            @RequestParam(value = "raison", defaultValue = "Annulé par l'administrateur") String raison,
            RedirectAttributes redirectAttributes) {
        try {
            Reservation reservation = reservationService.getReservationById(id);

            // Annuler la réservation
            reservation = reservationService.refuseReservation(id, raison);

            redirectAttributes.addFlashAttribute("success",
                    "Réservation #" + reservation.getReference() + " annulée avec succès");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    "Erreur: " + e.getMessage());
        }

        return "redirect:/reservations/admin";
    }

    @PostMapping("/admin/changer-statut/{id}")
    public String changerStatutReservation(@PathVariable Long id,
            @RequestParam String nouveauStatut,
            @RequestParam(required = false, defaultValue = "") String raison,
            RedirectAttributes redirectAttributes) {
        try {
            Reservation reservation = reservationService.getReservationById(id);

            StatutReservation statut = StatutReservation.valueOf(nouveauStatut);

            switch (statut) {
                case CONFIRMEE:
                    reservation = reservationService.confirmReservation(id);
                    redirectAttributes.addFlashAttribute("success",
                            "Réservation #" + reservation.getReference() + " confirmée avec succès");
                    break;
                case ANNULEE:
                    reservation = reservationService.refuseReservation(id,
                            raison.isEmpty() ? "Annulé par l'administrateur" : raison);
                    redirectAttributes.addFlashAttribute("success",
                            "Réservation #" + reservation.getReference() + " annulée avec succès");
                    break;
                default:
                    redirectAttributes.addFlashAttribute("error",
                            "Statut invalide");
                    return "redirect:/reservations/admin";
            }

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    "Erreur: " + e.getMessage());
        }

        return "redirect:/reservations/admin";
    }

    private long countReservationsToday() {
        // Implémentation simplifiée
        return reservationService.getReservationsPage(PageRequest.of(0, 1000))
                .getContent()
                .stream()
                .filter(r -> r.getDateArrivee().equals(LocalDate.now()))
                .count();
    }

    public boolean checkDisponibilite(Long chambreId, LocalDate dateArrivee, LocalDate dateDepart) {
        System.out.println("DEBUG: Vérification disponibilité chambre " + chambreId);

        // Vérifier que la chambre existe et est disponible
        Optional<Chambre> chambreOpt = chambreRepository.findById(chambreId);
        if (chambreOpt.isEmpty()) {
            System.out.println("DEBUG: Chambre non trouvée");
            return false;
        }

        Chambre chambre = chambreOpt.get();
        System.out.println("DEBUG: Chambre disponible? " + chambre.getDisponible());

        // TEMPORAIRE : Pour tester, retourner toujours true
        // À REMPLACER par la vérification réelle plus tard
        return true;

        // Code original commenté :
        // return !reservationRepository.isChambreReservee(chambreId, dateArrivee,
        // dateDepart);
    }

    @GetMapping("/api/prix-calcule")
    @ResponseBody
    public Object calculatePrice(
            @RequestParam Long chambreId,
            @RequestParam LocalDate dateArrivee,
            @RequestParam LocalDate dateDepart,
            @RequestParam(defaultValue = "1") Integer personnes) {

        BigDecimal prix = reservationService.calculatePrixTotal(chambreId, dateArrivee, dateDepart, personnes);

        return Map.of(
                "prixTotal", prix,
                "chambreId", chambreId,
                "dateArrivee", dateArrivee,
                "dateDepart", dateDepart,
                "personnes", personnes);
    }
}