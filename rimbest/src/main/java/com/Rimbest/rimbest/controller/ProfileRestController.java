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
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/api/profile")
public class ProfileRestController {

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // GET /api/profile
    @GetMapping
    public ResponseEntity<UserProfileDTO> getProfile(Authentication authentication) {
        User user = userService.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));

        UserProfileDTO profileDTO = new UserProfileDTO();
        profileDTO.setNom(user.getNom());
        profileDTO.setEmail(user.getEmail());
        profileDTO.setTelephone(user.getTelephone());
        profileDTO.setAdresse(user.getAdresse());

        if (user instanceof Client) {
            // Déjà géré par User
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

        return ResponseEntity.ok(profileDTO);
    }

    // PUT /api/profile
    @PutMapping
    public ResponseEntity<UserProfileDTO> updateProfile(
            Authentication authentication,
            @Valid @RequestBody UserProfileDTO profileDTO) {

        User user = userService.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));

        user.setNom(profileDTO.getNom());
        user.setEmail(profileDTO.getEmail());
        user.setTelephone(profileDTO.getTelephone());
        user.setAdresse(profileDTO.getAdresse());

        if (user instanceof Client) {
            // Déjà géré par les champs communs de User
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

        userService.updateUser(user.getId(), user);
        return ResponseEntity.ok(profileDTO);
    }

    // PUT /api/profile/password
    @PutMapping("/password")
    public ResponseEntity<Void> changePassword(
            Authentication authentication,
            @Valid @RequestBody ChangePasswordDTO passwordDTO) {

        if (!passwordDTO.getNewPassword().equals(passwordDTO.getConfirmPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Les mots de passe ne correspondent pas");
        }

        User user = userService.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));

        if (!passwordEncoder.matches(passwordDTO.getCurrentPassword(), user.getMotDePasse())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mot de passe actuel incorrect");
        }

        userService.changePassword(user.getId(), passwordDTO.getNewPassword());
        return ResponseEntity.ok().build();
    }
}
