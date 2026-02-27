package com.Rimbest.rimbest.controller;

import com.Rimbest.rimbest.model.Client;
import com.Rimbest.rimbest.model.Partenaire;
import com.Rimbest.rimbest.model.User;
import com.Rimbest.rimbest.model.Administrator;
import com.Rimbest.rimbest.model.dto.UserProfileDTO;
import com.Rimbest.rimbest.model.dto.ChangePasswordDTO;
import com.Rimbest.rimbest.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/profile")
public class ProfileController {

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // Afficher le profil de l'utilisateur connecté
    @GetMapping
    public String viewProfile(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();

        User user = userService.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        // Créer un DTO à partir de l'utilisateur
        UserProfileDTO profileDTO = new UserProfileDTO();
        profileDTO.setNom(user.getNom());
        profileDTO.setEmail(user.getEmail());

        // Remplir les champs spécifiques selon le type d'utilisateur
        if (user instanceof Client) {
            Client client = (Client) user;
            profileDTO.setTelephone(client.getTelephone());
            profileDTO.setAdresse(client.getAdresse());
        } else if (user instanceof Partenaire) {
            Partenaire partenaire = (Partenaire) user;
            profileDTO.setNomEntreprise(partenaire.getNomEntreprise());
            profileDTO.setSiret(partenaire.getSiret());
            profileDTO.setAdresseEntreprise(partenaire.getAdresseEntreprise());
            profileDTO.setTelephonePro(partenaire.getTelephonePro());
            profileDTO.setSiteWeb(partenaire.getSiteWeb());
            profileDTO.setDescription(partenaire.getDescription());
        } else if (user instanceof Administrator) {
            Administrator admin = (Administrator) user;
            profileDTO.setDepartement(admin.getDepartement());
        }

        model.addAttribute("profile", profileDTO);
        model.addAttribute("user", user);
        model.addAttribute("isClient", user instanceof Client);
        model.addAttribute("isPartenaire", user instanceof Partenaire);
        model.addAttribute("isAdmin", user instanceof Administrator);

        return "user/profile";
    }

    // Afficher le formulaire d'édition du profil
    @GetMapping("/edit")
    public String editProfileForm(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();

        User user = userService.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        UserProfileDTO profileDTO = new UserProfileDTO();
        profileDTO.setNom(user.getNom());
        profileDTO.setEmail(user.getEmail());

        if (user instanceof Client) {
            Client client = (Client) user;
            profileDTO.setTelephone(client.getTelephone());
            profileDTO.setAdresse(client.getAdresse());
        } else if (user instanceof Partenaire) {
            Partenaire partenaire = (Partenaire) user;
            profileDTO.setNomEntreprise(partenaire.getNomEntreprise());
            profileDTO.setSiret(partenaire.getSiret());
            profileDTO.setAdresseEntreprise(partenaire.getAdresseEntreprise());
            profileDTO.setTelephonePro(partenaire.getTelephonePro());
            profileDTO.setSiteWeb(partenaire.getSiteWeb());
            profileDTO.setDescription(partenaire.getDescription());
        } else if (user instanceof Administrator) {
            Administrator admin = (Administrator) user;
            profileDTO.setDepartement(admin.getDepartement());
        }

        model.addAttribute("profile", profileDTO);
        model.addAttribute("user", user);

        return "user/edit-profile";
    }

    // Mettre à jour le profil
    @PostMapping("/update")
    public String updateProfile(@Valid @ModelAttribute("profile") UserProfileDTO profileDTO,
            BindingResult result,
            Model model,
            RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String email = auth.getName();
            User user = userService.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
            model.addAttribute("user", user);
            return "user/edit-profile";
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();

        User user = userService.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        // Mettre à jour les informations communes
        user.setNom(profileDTO.getNom());
        user.setEmail(profileDTO.getEmail());

        // Mettre à jour les informations spécifiques
        if (user instanceof Client) {
            Client client = (Client) user;
            client.setTelephone(profileDTO.getTelephone());
            client.setAdresse(profileDTO.getAdresse());
        } else if (user instanceof Partenaire) {
            Partenaire partenaire = (Partenaire) user;
            partenaire.setNomEntreprise(profileDTO.getNomEntreprise());
            partenaire.setSiret(profileDTO.getSiret());
            partenaire.setAdresseEntreprise(profileDTO.getAdresseEntreprise());
            partenaire.setTelephonePro(profileDTO.getTelephonePro());
            partenaire.setSiteWeb(profileDTO.getSiteWeb());
            partenaire.setDescription(profileDTO.getDescription());
        } else if (user instanceof Administrator) {
            Administrator admin = (Administrator) user;
            admin.setDepartement(profileDTO.getDepartement());
        }

        // Sauvegarder les modifications
        userService.updateUser(user.getId(), user);

        redirectAttributes.addFlashAttribute("success", "Profil mis à jour avec succès!");
        return "redirect:/profile";
    }

    // Afficher le formulaire de changement de mot de passe
    @GetMapping("/change-password")
    public String changePasswordForm(Model model) {
        model.addAttribute("passwordDTO", new ChangePasswordDTO());
        return "user/change-password";
    }

    // Changer le mot de passe
    @PostMapping("/change-password")
    public String changePassword(@Valid @ModelAttribute("passwordDTO") ChangePasswordDTO passwordDTO,
            BindingResult result,
            Model model,
            RedirectAttributes redirectAttributes) {

        // Validation personnalisée
        if (!passwordDTO.getNewPassword().equals(passwordDTO.getConfirmPassword())) {
            result.rejectValue("confirmPassword", "error.passwordDTO", "Les mots de passe ne correspondent pas");
        }

        if (result.hasErrors()) {
            return "user/change-password";
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();

        User user = userService.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        // Vérifier l'ancien mot de passe
        if (!passwordEncoder.matches(passwordDTO.getCurrentPassword(), user.getMotDePasse())) {
            result.rejectValue("currentPassword", "error.passwordDTO", "Mot de passe actuel incorrect");
            return "user/change-password";
        }

        // IMPORTANT : Appeler une méthode spécialisée pour le changement de mot de
        // passe
        // plutôt que updateUser() qui pourrait écraser d'autres données
        userService.changePassword(user.getId(), passwordDTO.getNewPassword());

        redirectAttributes.addFlashAttribute("success", "Mot de passe changé avec succès!");
        return "redirect:/profile";
    }

    @GetMapping("/test-password")
    @ResponseBody
    public String testPassword() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();

        User user = userService.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        // Testez avec un mot de passe connu
        String testPassword = "test123";
        String hashed = passwordEncoder.encode(testPassword);

        return String.format(
                "Email: %s<br>" +
                        "Mot de passe actuel en base (premiers 20 chars): %s<br>" +
                        "Test hash pour '%s': %s<br>" +
                        "Matches? %s",
                email,
                user.getMotDePasse().substring(0, Math.min(20, user.getMotDePasse().length())),
                testPassword,
                hashed.substring(0, Math.min(20, hashed.length())),
                passwordEncoder.matches(testPassword, user.getMotDePasse()));
    }

}