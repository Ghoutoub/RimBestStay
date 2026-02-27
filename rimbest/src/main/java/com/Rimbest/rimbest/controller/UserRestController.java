package com.Rimbest.rimbest.controller;

import com.Rimbest.rimbest.model.ERole;
import com.Rimbest.rimbest.model.User;
import com.Rimbest.rimbest.model.dto.StatusUpdateRequest;
import com.Rimbest.rimbest.model.dto.UserRegistrationDTO;
import com.Rimbest.rimbest.model.dto.UserResponse;
import com.Rimbest.rimbest.model.dto.UserUpdateRequest;
import com.Rimbest.rimbest.repository.UserRepository;
import com.Rimbest.rimbest.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/users")
@PreAuthorize("hasRole('ADMIN')")
public class UserRestController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    // GET /api/admin/users?page=0&size=10&search=john&role=CLIENT&active=true
    @GetMapping
    public ResponseEntity<Page<UserResponse>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) ERole role,
            @RequestParam(required = false) Boolean active) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<User> userPage;

        // Si un search est fourni, on cherche par nom ou email
        if (search != null && !search.trim().isEmpty()) {
            // On peut améliorer avec une requête plus sophistiquée
            userPage = userRepository.findByNomContainingIgnoreCaseOrEmailContainingIgnoreCase(
                    search, search, pageable);
        } else {
            userPage = userRepository.findAll(pageable);
        }

        // Filtrage par rôle et actif (en mémoire, car notre repository ne les supporte
        // pas encore)
        // Pour une meilleure performance, il faudrait ajouter des méthodes dédiées dans
        // le repository
        List<User> filtered = userPage.getContent().stream()
                .filter(user -> role == null || user.getRoles().stream().anyMatch(r -> r.getName() == role))
                .filter(user -> active == null || (user.getActif() != null && user.getActif().equals(active)))
                .collect(Collectors.toList());

        // Reconstruire une page (simplifié)
        Page<UserResponse> responsePage = new org.springframework.data.domain.PageImpl<>(
                filtered.stream().map(UserResponse::fromUser).collect(Collectors.toList()),
                pageable,
                userPage.getTotalElements() // approximation
        );

        return ResponseEntity.ok(responsePage);
    }

    // GET /api/admin/users/{id}
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        User user = userService.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utilisateur non trouvé"));
        return ResponseEntity.ok(UserResponse.fromUser(user));
    }

    // POST /api/admin/users
    @PostMapping
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody UserRegistrationDTO request) {
        User user;
        switch (request.getRole()) {
            case ROLE_CLIENT:
                com.Rimbest.rimbest.model.Client client = new com.Rimbest.rimbest.model.Client();
                client.setNom(request.getNom());
                client.setEmail(request.getEmail());
                client.setMotDePasse(request.getMotDePasse());
                client.setTelephone(request.getTelephone());
                client.setAdresse(request.getAdresse());
                client.setActif(true); // <-- Ajout
                user = userService.registerClient(client);
                break;
            case ROLE_PARTENAIRE:
                com.Rimbest.rimbest.model.Partenaire partenaire = new com.Rimbest.rimbest.model.Partenaire();
                partenaire.setNom(request.getNom());
                partenaire.setEmail(request.getEmail());
                partenaire.setMotDePasse(request.getMotDePasse());
                partenaire.setNomEntreprise(request.getNomEntreprise());
                partenaire.setSiret(request.getSiret());
                partenaire.setActif(true); // <-- Ajout
                user = userService.registerPartenaire(partenaire);
                break;
            case ROLE_ADMIN:
                com.Rimbest.rimbest.model.Administrator admin = new com.Rimbest.rimbest.model.Administrator();
                admin.setNom(request.getNom());
                admin.setEmail(request.getEmail());
                admin.setMotDePasse(request.getMotDePasse());
                admin.setDepartement(request.getDepartement());
                admin.setActif(true); // <-- Ajout
                user = userService.registerAdministrator(admin);
                break;
            default:
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Rôle non supporté");
        }
        return new ResponseEntity<>(UserResponse.fromUser(user), HttpStatus.CREATED);
    }

    // PUT /api/admin/users/{id}
    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateUser(@PathVariable Long id,
            @Valid @RequestBody UserUpdateRequest request) {
        User existing = userService.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utilisateur non trouvé"));

        // Mise à jour des champs communs
        existing.setNom(request.getNom());
        existing.setEmail(request.getEmail());
        existing.setActif(request.getActif() != null ? request.getActif() : existing.getActif());
        existing.setTelephone(request.getTelephone());
        existing.setAdresse(request.getAdresse());

        // Mise à jour des champs spécifiques selon le type réel
        if (existing instanceof com.Rimbest.rimbest.model.Client) {
            // Déjà géré par User
        } else if (existing instanceof com.Rimbest.rimbest.model.Partenaire) {
            com.Rimbest.rimbest.model.Partenaire partenaire = (com.Rimbest.rimbest.model.Partenaire) existing;
            partenaire.setNomEntreprise(request.getNomEntreprise());
            partenaire.setSiret(request.getSiret());
            partenaire.setAdresseEntreprise(request.getAdresseEntreprise());
            partenaire.setTelephonePro(request.getTelephonePro());
            partenaire.setSiteWeb(request.getSiteWeb());
            partenaire.setDescription(request.getDescription());
            if (request.getVerified() != null)
                partenaire.setVerified(request.getVerified());
            if (request.getCommissionRate() != null)
                partenaire.setCommissionRate(request.getCommissionRate());
        } else if (existing instanceof com.Rimbest.rimbest.model.Administrator) {
            com.Rimbest.rimbest.model.Administrator admin = (com.Rimbest.rimbest.model.Administrator) existing;
            admin.setDepartement(request.getDepartement());
        }

        User updated = userService.updateUser(id, existing);
        return ResponseEntity.ok(UserResponse.fromUser(updated));
    }

    // DELETE /api/admin/users/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        // Empêcher la suppression de son propre compte (optionnel)
        // Ici, on pourrait vérifier que l'utilisateur connecté n'est pas celui qu'on
        // supprime
        boolean deleted = userService.deleteUser(id);
        if (!deleted) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Utilisateur non trouvé");
        }
        return ResponseEntity.noContent().build();
    }

    // PATCH /api/admin/users/{id}/status (pour activer/désactiver)
    @PatchMapping("/{id}/status")
    public ResponseEntity<UserResponse> updateUserStatus(@PathVariable Long id,
            @RequestBody StatusUpdateRequest request) {
        User user = userService.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Utilisateur non trouvé"));

        user.setActif(request.isActivate());
        User updated = userService.updateUser(id, user);
        return ResponseEntity.ok(UserResponse.fromUser(updated));
    }
}