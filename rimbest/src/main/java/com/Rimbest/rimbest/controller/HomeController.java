package com.Rimbest.rimbest.controller;

import com.Rimbest.rimbest.model.ERole;
import com.Rimbest.rimbest.model.Hotel;
import com.Rimbest.rimbest.model.User;
import com.Rimbest.rimbest.service.HotelService;
import com.Rimbest.rimbest.service.UserService;
import com.Rimbest.rimbest.service.ReservationService;
import com.Rimbest.rimbest.model.Reservation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.math.BigDecimal;
import java.util.List;

@Controller
public class HomeController {

    @Autowired
    private HotelService hotelService;

    @Autowired
    private UserService userService;
    
    @Autowired
    private ReservationService reservationService;

    @GetMapping("/")
    public String home() {
        return "home";
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        String role = auth.getAuthorities().toString();

        model.addAttribute("username", username);
        model.addAttribute("role", role);

        // Récupérer l'utilisateur connecté
        User currentUser = userService.findByEmail(username).orElse(null);

        if (role.contains("ROLE_ADMIN")) {
            // Statistiques pour admin
            model.addAttribute("totalHotels", hotelService.countAllHotels());
            model.addAttribute("recentHotels", hotelService.findRecentHotels(5));

            // NOUVEAU : Statistiques utilisateurs
            model.addAttribute("totalUsers", userService.countAllUsers());
            model.addAttribute("clientsCount", userService.countUsersByRole(ERole.ROLE_CLIENT));
            model.addAttribute("partenairesCount", userService.countUsersByRole(ERole.ROLE_PARTENAIRE));
            model.addAttribute("adminsCount", userService.countUsersByRole(ERole.ROLE_ADMIN));
            model.addAttribute("recentUsers", userService.findRecentUsers(10));

            // Ajouter dans la méthode dashboard() pour les statistiques

            // Statistiques réservations
            model.addAttribute("totalReservations", reservationService.countTotalReservations());
            model.addAttribute("reservationsToday", reservationService.countReservationsToday());
            model.addAttribute("revenueMonth", reservationService.getRevenueForCurrentMonth());
            
            // Réservations en attente
            List<Reservation> allReservations = reservationService.getReservationsPage(org.springframework.data.domain.PageRequest.of(0, 1000)).getContent();
            long pendingReservations = allReservations.stream()
                    .filter(r -> r.getStatut() == com.Rimbest.rimbest.model.StatutReservation.EN_ATTENTE)
                    .count();
            model.addAttribute("pendingReservations", pendingReservations);

            return "admin/dashboard";

        } else if (role.contains("ROLE_PARTENAIRE")) {
            // Statistiques pour partenaire
            if (currentUser != null) {
                List<Hotel> myHotels = hotelService.findHotelsByPartenaire(currentUser);
                model.addAttribute("myHotels", myHotels);
                model.addAttribute("myHotelsCount", myHotels.size());
                model.addAttribute("totalChambres", hotelService.countChambresByPartenaire(currentUser));
                model.addAttribute("availableChambres", hotelService.countAvailableChambresByPartenaire(currentUser));
                
                // Statistiques de réservations pour le partenaire
                long totalReservations = reservationService.getReservationsByPartenaire(currentUser).size();
                long pendingReservations = reservationService.getReservationsByPartenaire(currentUser).stream()
                        .filter(r -> r.getStatut() == com.Rimbest.rimbest.model.StatutReservation.EN_ATTENTE)
                        .count();
                
                // Calcul du revenu (simplifié)
                BigDecimal totalRevenue = reservationService.getReservationsByPartenaire(currentUser).stream()
                        .filter(r -> r.getStatut() == com.Rimbest.rimbest.model.StatutReservation.CONFIRMEE)
                        .map(Reservation::getPrixTotal)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                
                model.addAttribute("totalReservations", totalReservations);
                model.addAttribute("pendingReservations", pendingReservations);
                model.addAttribute("totalRevenue", totalRevenue);
                model.addAttribute("reservationGrowth", 12.5); // Valeur statique pour le moment
                model.addAttribute("monthlyRevenue", totalRevenue);
            }
            return "partenaire/dashboard";

        } else if (role.contains("ROLE_CLIENT")) {
            // Statistiques pour client
            if (currentUser != null) {
                long totalReservations = reservationService.countReservationsByClient(currentUser);
                long pendingReservations = reservationService.getReservationsByClient(currentUser).stream()
                        .filter(r -> r.getStatut() == com.Rimbest.rimbest.model.StatutReservation.EN_ATTENTE)
                        .count();
                BigDecimal totalRevenue = reservationService.getRevenueByClient(currentUser);
                List<Reservation> recentReservations = reservationService.getRecentReservationsByClient(currentUser, 5);
                
                model.addAttribute("totalReservations", totalReservations);
                model.addAttribute("pendingReservations", pendingReservations);
                model.addAttribute("totalRevenue", totalRevenue);
                model.addAttribute("recentReservations", recentReservations);
            }
            return "client/dashboard";
        }

        return "dashboard";
    }
}