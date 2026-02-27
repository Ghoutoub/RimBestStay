package com.Rimbest.rimbest.controller;

import com.Rimbest.rimbest.model.*;
import com.Rimbest.rimbest.model.dto.ReservationRequestDTO;
import com.Rimbest.rimbest.model.dto.UserRegistrationDTO;
import com.Rimbest.rimbest.model.dto.UserUpdateDTO;
import com.Rimbest.rimbest.service.UserService;
import com.Rimbest.rimbest.repository.UserRepository;
import com.Rimbest.rimbest.service.ReservationService;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/users")
public class AdminUserController {

    @Autowired
    private UserService userService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ReservationService reservationService;

    // DEBUG: Méthode de test pour diagnostiquer les utilisateurs
    @GetMapping("/debug")
    public String debugUsers(Model model) {
        System.out.println("=== DEBUG DES UTILISATEURS ===");

        // 1. Test findAllUsers()
        System.out.println("\n1. Test findAllUsers():");
        List<User> users = userService.findAllUsers();
        System.out.println("users != null: " + (users != null));
        System.out.println("Nombre d'utilisateurs: " + (users != null ? users.size() : "null"));

        if (users != null && !users.isEmpty()) {
            for (int i = 0; i < Math.min(5, users.size()); i++) {
                User user = users.get(i);
                System.out.println((i + 1) + ". " + user.getNom() + " - " + user.getEmail() +
                        " - Actif: " + user.getActif() +
                        " - Rôles: " + (user.getRoles() != null ? user.getRoles().size() : "null"));
            }
        }

        // 2. Test countAllUsers()
        System.out.println("\n2. Test countAllUsers():");
        long total = userService.countAllUsers();
        System.out.println("Total: " + total);

        // 3. Test des comptes par rôle
        System.out.println("\n3. Test des comptes par rôle:");
        System.out.println("Clients: " + userService.countUsersByRole(ERole.ROLE_CLIENT));
        System.out.println("Partenaires: " + userService.countUsersByRole(ERole.ROLE_PARTENAIRE));
        System.out.println("Admins: " + userService.countUsersByRole(ERole.ROLE_ADMIN));

        // 4. Vérifier la base de données directement
        System.out.println("\n4. Test du repository:");
        long countFromRepo = userRepository.count();
        System.out.println("userRepository.count(): " + countFromRepo);

        // Ajouter les données au modèle pour l'affichage
        model.addAttribute("users", users != null ? users : new ArrayList<>());
        model.addAttribute("totalFromService", total);
        model.addAttribute("totalFromRepo", countFromRepo);

        return "admin/debug-users";
    }

    // Liste des utilisateurs avec filtres - VERSION CORRIGÉE
    @GetMapping
    public String listUsers(Model model,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) ERole role,
            @RequestParam(required = false) Boolean active) {

        List<User> users;

        try {
            // 1. Récupérer les utilisateurs selon les filtres
            if (search != null && !search.trim().isEmpty()) {
                users = userService.searchUsers(search);
            } else if (role != null) {
                users = userService.findUsersByRole(role);
            } else {
                users = userService.findAllUsers();
            }

            // 2. Filtrer par statut actif si spécifié
            if (active != null && users != null) {
                users = users.stream()
                        .filter(user -> user.getActif() != null && user.getActif().equals(active))
                        .collect(Collectors.toList());
            }

            // 3. Assurer que users n'est jamais null
            if (users == null) {
                users = Collections.emptyList();
                System.out.println("ATTENTION: La liste des utilisateurs est null!");
            }

            System.out.println("DEBUG: Nombre d'utilisateurs dans le contrôleur: " + users.size());
            if (!users.isEmpty()) {
                System.out.println("DEBUG: Premier utilisateur: " + users.get(0).getNom());
                System.out.println("DEBUG: Rôles du premier utilisateur: " +
                        (users.get(0).getRoles() != null ? users.get(0).getRoles().size() : "null"));
            }

        } catch (Exception e) {
            System.err.println("ERREUR dans listUsers: " + e.getMessage());
            e.printStackTrace();
            users = Collections.emptyList();
        }

        model.addAttribute("users", users);
        model.addAttribute("roles", ERole.values());
        model.addAttribute("totalUsers", userService.countAllUsers());
        model.addAttribute("clientsCount", userService.countUsersByRole(ERole.ROLE_CLIENT));
        model.addAttribute("partenairesCount", userService.countUsersByRole(ERole.ROLE_PARTENAIRE));
        model.addAttribute("adminsCount", userService.countUsersByRole(ERole.ROLE_ADMIN));
        model.addAttribute("search", search);
        model.addAttribute("selectedRole", role);
        model.addAttribute("selectedActive", active);

        return "admin/users";
    }

    // AJOUTEZ CES MÉTHODES AU CONTROLEUR :

    // Actions groupées
    @PostMapping("/bulk-activate")
    @ResponseBody
    public ResponseEntity<?> bulkActivate(@RequestBody Map<String, List<Long>> request) {
        try {
            List<Long> userIds = request.get("userIds");
            if (userIds != null) {
                for (Long userId : userIds) {
                    userService.activateUser(userId);
                }
            }
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }

    @PostMapping("/bulk-deactivate")
    @ResponseBody
    public ResponseEntity<?> bulkDeactivate(@RequestBody Map<String, List<Long>> request) {
        try {
            List<Long> userIds = request.get("userIds");
            if (userIds != null) {
                for (Long userId : userIds) {
                    userService.deactivateUser(userId);
                }
            }
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }

    @PostMapping("/bulk-delete")
    @ResponseBody
    public ResponseEntity<?> bulkDelete(@RequestBody Map<String, List<Long>> request) {
        try {
            List<Long> userIds = request.get("userIds");
            if (userIds != null) {
                for (Long userId : userIds) {
                    // Ne pas supprimer l'admin principal (ID 1)
                    if (userId != 1) {
                        userService.deleteUser(userId);
                    }
                }
            }
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }

    // Toggle status pour un utilisateur

    @PostMapping("/{id}/toggle-status")
    public String toggleUserStatus(@PathVariable Long id,
            @RequestParam boolean activate,
            RedirectAttributes redirectAttributes) {
        try {
            User user = userService.findById(id)
                    .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

            // Vérifier si l'utilisateur n'essaie pas de se désactiver lui-même
            // (Vous devrez adapter ce code selon votre système d'authentification)
            /*
             * Authentication authentication =
             * SecurityContextHolder.getContext().getAuthentication();
             * String currentUsername = authentication.getName();
             * if (currentUsername.equals(user.getEmail())) {
             * redirectAttributes.addFlashAttribute("error",
             * "Vous ne pouvez pas modifier votre propre statut.");
             * return "redirect:/admin/users/" + id;
             * }
             */

            if (activate) {
                user.setActif(true);
                userService.updateUser(id, user);
                redirectAttributes.addFlashAttribute("success", "Utilisateur activé avec succès");
            } else {
                user.setActif(false);
                userService.updateUser(id, user);
                redirectAttributes.addFlashAttribute("success", "Utilisateur désactivé avec succès");
            }

        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", "Erreur: " + e.getMessage());
        }

        return "redirect:/admin/users/" + id;
    }

    // Vue détaillée d'un utilisateur - DOIT ÊTRE APRÈS LES MÉTHODES POST
    // SPÉCIFIQUES
    @GetMapping("/{id}")
    public String viewUser(@PathVariable Long id, Model model) {
        User user = userService.findById(id)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé avec ID: " + id));

        model.addAttribute("user", user);
        model.addAttribute("isClient", user instanceof Client);
        model.addAttribute("isPartenaire", user instanceof Partenaire);
        model.addAttribute("isAdmin", user instanceof Administrator);

        return "admin/user-details";
    }

    // Formulaire de création d'utilisateur
    @GetMapping("/create")
    public String createUserForm(Model model) {
        model.addAttribute("userDTO", new UserRegistrationDTO());
        model.addAttribute("roles", Arrays.asList(ERole.values()));
        return "admin/create-user";
    }

    // Création d'un utilisateur
    @PostMapping("/create")
    public String createUser(@Valid @ModelAttribute("userDTO") UserRegistrationDTO userDTO,
            BindingResult result,
            Model model,
            RedirectAttributes redirectAttributes) {

        // Validation personnalisée
        if (!userDTO.getMotDePasse().equals(userDTO.getConfirmPassword())) {
            result.rejectValue("confirmPassword", "error.userDTO", "Les mots de passe ne correspondent pas");
        }

        if (result.hasErrors()) {
            model.addAttribute("roles", Arrays.asList(ERole.values()));
            return "admin/create-user";
        }

        try {
            User user;

            switch (userDTO.getRole()) {
                case ROLE_CLIENT:
                    Client client = new Client();
                    client.setNom(userDTO.getNom());
                    client.setEmail(userDTO.getEmail());
                    client.setMotDePasse(userDTO.getMotDePasse());
                    client.setTelephone(userDTO.getTelephone());
                    client.setAdresse(userDTO.getAdresse());
                    user = userService.registerClient(client);
                    break;

                case ROLE_PARTENAIRE:
                    Partenaire partenaire = new Partenaire();
                    partenaire.setNom(userDTO.getNom());
                    partenaire.setEmail(userDTO.getEmail());
                    partenaire.setMotDePasse(userDTO.getMotDePasse());
                    partenaire.setNomEntreprise(userDTO.getNomEntreprise());
                    partenaire.setSiret(userDTO.getSiret());
                    user = userService.registerPartenaire(partenaire);
                    break;

                case ROLE_ADMIN:
                    Administrator admin = new Administrator();
                    admin.setNom(userDTO.getNom());
                    admin.setEmail(userDTO.getEmail());
                    admin.setMotDePasse(userDTO.getMotDePasse());
                    admin.setDepartement(userDTO.getDepartement());
                    user = userService.registerAdministrator(admin);
                    break;

                default:
                    throw new RuntimeException("Rôle non supporté");
            }

            redirectAttributes.addFlashAttribute("success",
                    "Utilisateur créé avec succès (ID: " + user.getId() + ")");
            return "redirect:/admin/users";

        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("roles", Arrays.asList(ERole.values()));
            return "admin/create-user";
        }
    }

    // Formulaire d'édition d'utilisateur
    @GetMapping("/{id}/edit")
    public String editUserForm(@PathVariable Long id, Model model) {
        User user = userService.findById(id)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé avec ID: " + id));

        UserUpdateDTO userDTO = new UserUpdateDTO();
        userDTO.setNom(user.getNom());
        userDTO.setEmail(user.getEmail());
        userDTO.setActif(user.getActif());

        // Remplir les champs spécifiques
        if (user instanceof Client) {
            Client client = (Client) user;
            userDTO.setTelephone(client.getTelephone());
            userDTO.setAdresse(client.getAdresse());
        } else if (user instanceof Partenaire) {
            Partenaire partenaire = (Partenaire) user;
            userDTO.setNomEntreprise(partenaire.getNomEntreprise());
            userDTO.setSiret(partenaire.getSiret());
            userDTO.setAdresseEntreprise(partenaire.getAdresseEntreprise());
            userDTO.setTelephonePro(partenaire.getTelephonePro());
            userDTO.setSiteWeb(partenaire.getSiteWeb());
            userDTO.setDescription(partenaire.getDescription());
            userDTO.setVerified(partenaire.isVerified());
            userDTO.setCommissionRate(partenaire.getCommissionRate());
        } else if (user instanceof Administrator) {
            Administrator admin = (Administrator) user;
            userDTO.setDepartement(admin.getDepartement());
        }

        model.addAttribute("userDTO", userDTO);
        model.addAttribute("userId", id);
        model.addAttribute("userType", user.getClass().getSimpleName());

        return "admin/edit-user";
    }

    // Mise à jour d'un utilisateur
    @PostMapping("/{id}/update")
    public String updateUser(@PathVariable Long id,
            @Valid @ModelAttribute("userDTO") UserUpdateDTO userDTO,
            BindingResult result,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            User user = userService.findById(id)
                    .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
            model.addAttribute("userType", user.getClass().getSimpleName());
            return "admin/edit-user";
        }

        try {
            User user = userService.findById(id)
                    .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

            // Mettre à jour les champs communs
            user.setNom(userDTO.getNom());
            user.setEmail(userDTO.getEmail());
            user.setActif(userDTO.getActif());

            // Mettre à jour les champs spécifiques
            if (user instanceof Client) {
                Client client = (Client) user;
                client.setTelephone(userDTO.getTelephone());
                client.setAdresse(userDTO.getAdresse());
            } else if (user instanceof Partenaire) {
                Partenaire partenaire = (Partenaire) user;
                partenaire.setNomEntreprise(userDTO.getNomEntreprise());
                partenaire.setSiret(userDTO.getSiret());
                partenaire.setAdresseEntreprise(userDTO.getAdresseEntreprise());
                partenaire.setTelephonePro(userDTO.getTelephonePro());
                partenaire.setSiteWeb(userDTO.getSiteWeb());
                partenaire.setDescription(userDTO.getDescription());
                partenaire.setVerified(userDTO.isVerified());
                partenaire.setCommissionRate(userDTO.getCommissionRate());
            } else if (user instanceof Administrator) {
                Administrator admin = (Administrator) user;
                admin.setDepartement(userDTO.getDepartement());
            }

            userService.updateUser(id, user);

            redirectAttributes.addFlashAttribute("success", "Utilisateur mis à jour avec succès");
            return "redirect:/admin/users/" + id;

        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/users/" + id + "/edit";
        }
    }

    // Changer le rôle d'un utilisateur
    @PostMapping("/{id}/change-role")
    public String changeUserRole(@PathVariable Long id,
            @RequestParam ERole newRole,
            RedirectAttributes redirectAttributes) {
        try {
            userService.changeUserRole(id, newRole);
            redirectAttributes.addFlashAttribute("success",
                    "Rôle changé en " + newRole + " avec succès");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/admin/users/" + id;
    }

    // Supprimer un utilisateur (version améliorée)
    @PostMapping("/{id}/delete")
    public String deleteUser(@PathVariable Long id,
            RedirectAttributes redirectAttributes) {
        try {
            // Empêcher la suppression de l'utilisateur courant s'il est connecté
            User currentUser = getCurrentUser(); // Vous devez implémenter cette méthode

            if (currentUser != null && currentUser.getId().equals(id)) {
                redirectAttributes.addFlashAttribute("error",
                        "Vous ne pouvez pas supprimer votre propre compte.");
                return "redirect:/admin/users";
            }

            // Vérifier si c'est le dernier administrateur
            if (isLastAdmin(id)) {
                redirectAttributes.addFlashAttribute("error",
                        "Impossible de supprimer le dernier administrateur.");
                return "redirect:/admin/users";
            }

            boolean deleted = userService.deleteUser(id);
            if (deleted) {
                redirectAttributes.addFlashAttribute("success",
                        "Utilisateur supprimé avec succès");
            } else {
                redirectAttributes.addFlashAttribute("error",
                        "Utilisateur non trouvé");
            }
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error",
                    "Erreur lors de la suppression: " + e.getMessage());
        }

        return "redirect:/admin/users";
    }

    // Méthode pour vérifier si c'est le dernier administrateur
    private boolean isLastAdmin(Long userId) {
        User user = userService.findById(userId).orElse(null);
        if (user != null && user.getRoles().stream()
                .anyMatch(role -> role.getName() == ERole.ROLE_ADMIN)) {

            long adminCount = userService.countUsersByRole(ERole.ROLE_ADMIN);
            return adminCount <= 1;
        }
        return false;
    }

    // Méthode pour obtenir l'utilisateur courant (à adapter selon votre système
    // d'authentification)
    private User getCurrentUser() {
        // Exemple avec Spring Security
        // Authentication authentication =
        // SecurityContextHolder.getContext().getAuthentication();
        // String email = authentication.getName();
        // return userService.findByEmail(email).orElse(null);

        return null; // À implémenter
    }

    @PostMapping("/admin/confirmer/{id}")
    public String confirmerReservationAdmin(@PathVariable Long id,
            RedirectAttributes redirectAttributes) {
        try {
            // Admin peut tout confirmer sans vérification
            reservationService.confirmReservation(id);

            redirectAttributes.addFlashAttribute("success",
                    "Réservation #" + reservationService.getReservationById(id).getReference() +
                            " confirmée avec succès");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    "Erreur: " + e.getMessage());
        }

        return "redirect:/reservations/admin";
    }

    @PostMapping("/admin/modifier/{id}")
    public String modifierReservationAdmin(@PathVariable Long id,
            @ModelAttribute ReservationRequestDTO requestDTO,
            RedirectAttributes redirectAttributes) {
        try {
            // Admin peut modifier n'importe quelle réservation
            Reservation reservation = reservationService.updateReservation(id, requestDTO);

            redirectAttributes.addFlashAttribute("success",
                    "Réservation #" + reservation.getReference() + " modifiée");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    "Erreur de modification: " + e.getMessage());
        }

        return "redirect:/reservations/admin/details/" + id;
    }
}