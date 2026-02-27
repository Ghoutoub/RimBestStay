package com.Rimbest.rimbest.controller;

import com.Rimbest.rimbest.model.*;
import com.Rimbest.rimbest.model.dto.UserRegistrationDTO;
import com.Rimbest.rimbest.service.UserService;
import com.Rimbest.rimbest.service.ReservationService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Arrays;

@Controller
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;
    @Autowired
    private ReservationService reservationService;

    // Inscription partenaire
    @GetMapping("/register-partenaire")
    public String showPartenaireRegistrationForm(Model model) {
        model.addAttribute("partenaire", new Partenaire());
        return "auth/register-partenaire";
    }

    @PostMapping("/register-partenaire")
    public String registerPartenaire(@Valid @ModelAttribute("partenaire") Partenaire partenaire,
            BindingResult result,
            Model model,
            RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "auth/register-partenaire";
        }

        try {
            userService.registerPartenaire(partenaire);
            redirectAttributes.addFlashAttribute("success",
                    "Inscription partenaire réussie! En attente de vérification.");
            return "redirect:/login";
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            return "auth/register-partenaire";
        }
    }

    // Liste des utilisateurs (pour admin)
    @GetMapping("/list")
    public String listUsers(Model model,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String status) {

        model.addAttribute("users", userService.findAllUsers());
        model.addAttribute("totalUsers", userService.countAllUsers());
        model.addAttribute("clientsCount", userService.countUsersByRole(ERole.ROLE_CLIENT));
        model.addAttribute("partenairesCount", userService.countUsersByRole(ERole.ROLE_PARTENAIRE));
        model.addAttribute("adminsCount", userService.countUsersByRole(ERole.ROLE_ADMIN));

        return "admin/users";
    }

    // Détails d'un utilisateur
    @GetMapping("/details/{id}")
    public String viewUserDetails(@PathVariable Long id, Model model) {
        User user = userService.findById(id)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé avec ID: " + id));

        model.addAttribute("user", user);
        model.addAttribute("isClient", user instanceof Client);
        model.addAttribute("isPartenaire", user instanceof Partenaire);
        model.addAttribute("isAdmin", user instanceof Administrator);

        return "admin/user-details";
    }

    // Activer/désactiver un utilisateur
    @PostMapping("/toggle-status/{id}")
    public String toggleUserStatus(@PathVariable Long id,
            @RequestParam boolean activate,
            RedirectAttributes redirectAttributes) {
        try {
            if (activate) {
                userService.activateUser(id);
                redirectAttributes.addFlashAttribute("success", "Utilisateur activé avec succès");
            } else {
                userService.deactivateUser(id);
                redirectAttributes.addFlashAttribute("success", "Utilisateur désactivé avec succès");
            }
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/user/list";
    }

    @PostMapping("/partenaire/confirmer/{id}")
    public String confirmerReservation(@PathVariable Long id,
            RedirectAttributes redirectAttributes) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User partenaire = userService.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("Partenaire non trouvé"));

        // Vérifier que la réservation appartient à un hôtel du partenaire
        Reservation reservation = reservationService.getReservationById(id);

        if (!reservation.getChambre().getHotel().getPartenaire().getId().equals(partenaire.getId())) {
            redirectAttributes.addFlashAttribute("error",
                    "Vous n'êtes pas autorisé à confirmer cette réservation");
            return "redirect:/reservations/partenaire";
        }

        try {
            reservationService.confirmReservation(id);
            redirectAttributes.addFlashAttribute("success",
                    "Réservation confirmée avec succès");

            // Envoyer notification au client (email/sms)
            // notificationService.envoyerConfirmation(reservation);

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    "Erreur: " + e.getMessage());
        }

        return "redirect:/reservations/partenaire";
    }
}