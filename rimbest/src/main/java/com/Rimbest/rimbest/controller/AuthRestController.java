package com.Rimbest.rimbest.controller;

import com.Rimbest.rimbest.model.dto.LoginRequest;
import com.Rimbest.rimbest.model.dto.LoginResponse;
import com.Rimbest.rimbest.model.dto.RegisterClientRequest;
import com.Rimbest.rimbest.model.Client;
import com.Rimbest.rimbest.model.ERole;
import com.Rimbest.rimbest.model.Role;
import com.Rimbest.rimbest.repository.RoleRepository;
import com.Rimbest.rimbest.security.JwtUtils;
import com.Rimbest.rimbest.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
public class AuthRestController {

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    UserService userService;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    JwtUtils jwtUtils;

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        List<String> roles = userDetails.getAuthorities().stream()
                .map(item -> item.getAuthority())
                .collect(Collectors.toList());

        // Récupérer l'utilisateur complet pour obtenir l'ID et le nom
        var user = userService.findByEmail(userDetails.getUsername()).orElseThrow();

        return ResponseEntity.ok(new LoginResponse(jwt, user.getId(), user.getNom(), userDetails.getUsername(), roles));
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterClientRequest request) {
        return registerClient(request);
    }

    @PostMapping("/register/client")
    public ResponseEntity<?> registerClient(@Valid @RequestBody RegisterClientRequest request) {
        if (userService.emailExists(request.getEmail())) {
            return ResponseEntity.badRequest().body("Email déjà utilisé !");
        }

        Client client = new Client();
        client.setNom(request.getNom());
        client.setEmail(request.getEmail());
        client.setMotDePasse(request.getPassword());
        client.setTelephone(request.getTelephone());
        client.setAdresse(request.getAdresse());

        // Assigner le rôle CLIENT
        Role clientRole = roleRepository.findByName(ERole.ROLE_CLIENT)
                .orElseThrow(() -> new RuntimeException("Rôle CLIENT non trouvé"));
        Set<Role> roles = new HashSet<>();
        roles.add(clientRole);
        client.setRoles(roles);

        userService.registerClient(client); // ou directement userRepository.save(client) si vous préférez

        return ResponseEntity.ok(java.util.Collections.singletonMap("message", "Client enregistré avec succès"));
    }

    // Vous pouvez ajouter des endpoints similaires pour /register/partenaire et
    // /register/admin
}