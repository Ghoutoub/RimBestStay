package com.Rimbest.rimbest.service;

import com.Rimbest.rimbest.model.*;
import com.Rimbest.rimbest.repository.RoleRepository;
import com.Rimbest.rimbest.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserService implements IUserService, CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // Méthode existante
    @Transactional
    public User registerClient(Client client) {
        try {
            logger.info("Début de l'inscription pour l'email: {}", client.getEmail());

            // Vérifier si l'email existe déjà
            if (userRepository.existsByEmail(client.getEmail())) {
                logger.error("Échec inscription: Email déjà utilisé - {}", client.getEmail());
                throw new RuntimeException("Email déjà utilisé!");
            }

            logger.info("Email disponible: {}", client.getEmail());

            // Encoder le mot de passe
            String rawPassword = client.getMotDePasse();
            logger.info("Mot de passe à encoder (longueur: {})", rawPassword.length());

            String encodedPassword = passwordEncoder.encode(rawPassword);
            client.setMotDePasse(encodedPassword);
            logger.info("Mot de passe encodé avec succès");

            // Trouver ou créer le rôle CLIENT
            Role clientRole = roleRepository.findByName(ERole.ROLE_CLIENT)
                    .orElseGet(() -> {
                        logger.warn("Rôle CLIENT non trouvé, création...");
                        Role newRole = new Role(ERole.ROLE_CLIENT);
                        return roleRepository.save(newRole);
                    });

            logger.info("Rôle trouvé/créé: {}", clientRole.getName());

            // Assigner les rôles
            Set<Role> roles = new HashSet<>();
            roles.add(clientRole);
            client.setRoles(roles);
            logger.info("Rôle assigné au client");

            // Sauvegarder le client
            logger.info("Sauvegarde du client...");
            User savedUser = userRepository.save(client);

            logger.info("SUCCÈS: Client enregistré avec ID: {}", savedUser.getId());
            logger.info("Client nom: {}, email: {}", savedUser.getNom(), savedUser.getEmail());

            return savedUser;

        } catch (Exception e) {
            logger.error("ERREUR lors de l'inscription pour {}: {}", client.getEmail(), e.getMessage(), e);
            throw new RuntimeException("Erreur lors de l'inscription: " + e.getMessage());
        }
    }

    // NOUVELLE: Méthode pour trouver un utilisateur par email
    @Transactional(readOnly = true)
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    // NOUVELLE: Méthode pour enregistrer un partenaire
    @Transactional
    public User registerPartenaire(User partenaire, String nomEntreprise) {
        try {
            logger.info("Début de l'inscription partenaire: {}", partenaire.getEmail());

            if (userRepository.existsByEmail(partenaire.getEmail())) {
                throw new RuntimeException("Email déjà utilisé!");
            }

            // Encoder le mot de passe
            String encodedPassword = passwordEncoder.encode(partenaire.getMotDePasse());
            partenaire.setMotDePasse(encodedPassword);

            // Trouver ou créer le rôle PARTENAIRE
            Role partenaireRole = roleRepository.findByName(ERole.ROLE_PARTENAIRE)
                    .orElseGet(() -> {
                        Role newRole = new Role(ERole.ROLE_PARTENAIRE);
                        return roleRepository.save(newRole);
                    });

            Set<Role> roles = new HashSet<>();
            roles.add(partenaireRole);
            partenaire.setRoles(roles);

            return userRepository.save(partenaire);

        } catch (Exception e) {
            logger.error("Erreur inscription partenaire: {}", e.getMessage(), e);
            throw new RuntimeException("Erreur lors de l'inscription partenaire: " + e.getMessage());
        }
    }

    // NOUVELLE: Méthode pour récupérer tous les utilisateurs - VERSION CORRIGÉE
    @Transactional(readOnly = true)
    public List<User> findAllUsers() {
        try {
            logger.info("Début de findAllUsers()");

            // Essayer d'abord la méthode avec les rôles
            List<User> users = userRepository.findAllWithRolesNative();

            if (users == null || users.isEmpty()) {
                logger.warn("findAllWithRolesNative() a retourné null ou vide, utilisation de findAll()");

                // Si la première méthode échoue, utiliser findAll() standard
                users = userRepository.findAll();

                if (users != null && !users.isEmpty()) {
                    // Charger les rôles pour chaque utilisateur
                    for (User user : users) {
                        // Initialiser la collection si nécessaire
                        if (user.getRoles() == null) {
                            user.setRoles(new HashSet<>());
                        } else {
                            // Forcer le chargement des rôles
                            user.getRoles().size();
                        }
                    }
                }
            }

            logger.info("Nombre d'utilisateurs trouvés: {}", users != null ? users.size() : 0);
            return users != null ? users : Collections.emptyList();

        } catch (Exception e) {
            logger.error("ERREUR dans findAllUsers(): {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    // NOUVELLE: Méthode de secours pour récupérer tous les utilisateurs
    @Transactional(readOnly = true)
    public List<User> findAllUsersAlternative() {
        try {
            // Utiliser une approche plus simple
            List<User> users = userRepository.findAll();

            // Log pour déboguer
            logger.info("findAllUsersAlternative() - Nombre d'utilisateurs: {}", users.size());

            for (User user : users) {
                logger.info("User: {} - Email: {} - Actif: {}",
                        user.getNom(), user.getEmail(), user.getActif());
            }

            return users;
        } catch (Exception e) {
            logger.error("Erreur dans findAllUsersAlternative: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    // NOUVELLE: Méthode pour récupérer les utilisateurs par rôle
    @Transactional(readOnly = true)
    public List<User> findUsersByRole(ERole role) {
        Role roleEntity = roleRepository.findByName(role)
                .orElseThrow(() -> new RuntimeException("Rôle non trouvé: " + role));

        return userRepository.findByRolesContaining(roleEntity);
    }

    // NOUVELLE: Méthode pour trouver un utilisateur par ID
    @Transactional(readOnly = true)
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    // NOUVELLE: Méthode pour mettre à jour un utilisateur
    @Transactional
    public User updateUser(Long id, User userDetails) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé avec ID: " + id));

        // Mettre à jour les champs de base
        user.setNom(userDetails.getNom());
        user.setEmail(userDetails.getEmail());
        user.setActif(userDetails.getActif());
        user.setTelephone(userDetails.getTelephone());
        user.setAdresse(userDetails.getAdresse());

        // NE PAS mettre à jour le mot de passe ici (utilisez changePassword pour ça)
        // Le mot de passe ne doit pas être changé via cette méthode

        // Mettre à jour les champs spécifiques
        if (user instanceof Client && userDetails instanceof Client) {
            // Déjà géré par User
        } else if (user instanceof Partenaire && userDetails instanceof Partenaire) {
            Partenaire partenaire = (Partenaire) user;
            Partenaire newDetails = (Partenaire) userDetails;
            partenaire.setNomEntreprise(newDetails.getNomEntreprise());
            partenaire.setSiret(newDetails.getSiret());
            partenaire.setAdresseEntreprise(newDetails.getAdresseEntreprise());
            partenaire.setTelephonePro(newDetails.getTelephonePro());
            partenaire.setSiteWeb(newDetails.getSiteWeb());
            partenaire.setDescription(newDetails.getDescription());
            partenaire.setVerified(newDetails.isVerified());
            partenaire.setCommissionRate(newDetails.getCommissionRate());
        } else if (user instanceof Administrator && userDetails instanceof Administrator) {
            Administrator admin = (Administrator) user;
            Administrator newDetails = (Administrator) userDetails;
            admin.setDepartement(newDetails.getDepartement());
        }

        return userRepository.save(user);
    }

    // NOUVELLE: Méthode pour désactiver un utilisateur
    @Transactional
    public void deactivateUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé avec ID: " + id));

        user.setActif(false);
        userRepository.save(user);
    }

    // NOUVELLE: Méthode pour activer un utilisateur
    @Transactional
    public void activateUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé avec ID: " + id));

        user.setActif(true);
        userRepository.save(user);
    }

    // NOUVELLE: Méthode pour changer le rôle d'un utilisateur
    @Transactional
    public User changeUserRole(Long userId, ERole newRole) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé avec ID: " + userId));

        Role role = roleRepository.findByName(newRole)
                .orElseThrow(() -> new RuntimeException("Rôle non trouvé: " + newRole));

        Set<Role> roles = new HashSet<>();
        roles.add(role);
        user.setRoles(roles);

        return userRepository.save(user);
    }

    // NOUVELLE: Méthode pour vérifier si un email existe
    @Transactional(readOnly = true)
    public boolean emailExists(String email) {
        return userRepository.existsByEmail(email);
    }

    // NOUVELLE: Méthode pour compter tous les utilisateurs
    @Transactional(readOnly = true)
    public long countAllUsers() {
        return userRepository.count();
    }

    // NOUVELLE: Méthode pour compter les utilisateurs par rôle
    @Transactional(readOnly = true)
    public long countUsersByRole(ERole role) {
        Role roleEntity = roleRepository.findByName(role)
                .orElseThrow(() -> new RuntimeException("Rôle non trouvé: " + role));

        return userRepository.countByRolesContaining(roleEntity);
    }

    // NOUVELLE: Méthode pour récupérer l'utilisateur courant (pour tests)
    @Transactional(readOnly = true)
    public Optional<User> getCurrentUser(String email) {
        return userRepository.findByEmail(email);
    }

    // NOUVELLE: Méthode pour créer des utilisateurs de test
    @Transactional
    public void createTestUsers() {
        // Vérifier si des utilisateurs existent déjà
        if (userRepository.count() > 0) {
            logger.info("Des utilisateurs existent déjà, skip des utilisateurs de test");
            return;
        }

        logger.info("Création des utilisateurs de test...");

        // Créer les rôles s'ils n'existent pas
        Role adminRole = roleRepository.findByName(ERole.ROLE_ADMIN)
                .orElseGet(() -> roleRepository.save(new Role(ERole.ROLE_ADMIN)));

        Role partenaireRole = roleRepository.findByName(ERole.ROLE_PARTENAIRE)
                .orElseGet(() -> roleRepository.save(new Role(ERole.ROLE_PARTENAIRE)));

        Role clientRole = roleRepository.findByName(ERole.ROLE_CLIENT)
                .orElseGet(() -> roleRepository.save(new Role(ERole.ROLE_CLIENT)));

        // Créer un admin
        Administrator admin = new Administrator();
        admin.setNom("Admin RIMBest");
        admin.setEmail("admin@rimbest.com");
        admin.setMotDePasse(passwordEncoder.encode("admin123"));
        admin.setDepartement("Administration");

        Set<Role> adminRoles = new HashSet<>();
        adminRoles.add(adminRole);
        admin.setRoles(adminRoles);
        userRepository.save(admin);

        // Créer un partenaire
        User partenaire = new User();
        partenaire.setNom("Hotel Marrakech Palace");
        partenaire.setEmail("partenaire@hotel.com");
        partenaire.setMotDePasse(passwordEncoder.encode("password123"));

        Set<Role> partenaireRoles = new HashSet<>();
        partenaireRoles.add(partenaireRole);
        partenaire.setRoles(partenaireRoles);
        userRepository.save(partenaire);

        // Créer un client
        Client client = new Client();
        client.setNom("Ahmed Benani");
        client.setEmail("client@test.com");
        client.setMotDePasse(passwordEncoder.encode("password123"));
        client.setTelephone("+212 612 345 678");
        client.setAdresse("123 Rue Test, Casablanca");

        Set<Role> clientRoles = new HashSet<>();
        clientRoles.add(clientRole);
        client.setRoles(clientRoles);
        userRepository.save(client);

        logger.info("Utilisateurs de test créés avec succès");
    }

    // AJOUTEZ CES NOUVELLES MÉTHODES :

    @Transactional
    public User registerPartenaire(Partenaire partenaire) {
        try {
            logger.info("Début de l'inscription partenaire: {}", partenaire.getEmail());

            if (userRepository.existsByEmail(partenaire.getEmail())) {
                throw new RuntimeException("Email déjà utilisé!");
            }

            String encodedPassword = passwordEncoder.encode(partenaire.getMotDePasse());
            partenaire.setMotDePasse(encodedPassword);

            Role partenaireRole = roleRepository.findByName(ERole.ROLE_PARTENAIRE)
                    .orElseGet(() -> {
                        Role newRole = new Role(ERole.ROLE_PARTENAIRE);
                        return roleRepository.save(newRole);
                    });

            Set<Role> roles = new HashSet<>();
            roles.add(partenaireRole);
            partenaire.setRoles(roles);

            return userRepository.save(partenaire);

        } catch (Exception e) {
            logger.error("Erreur inscription partenaire: {}", e.getMessage(), e);
            throw new RuntimeException("Erreur lors de l'inscription partenaire: " + e.getMessage());
        }
    }

    @Transactional
    public User registerAdministrator(Administrator admin) {
        try {
            logger.info("Début de l'inscription admin: {}", admin.getEmail());

            if (userRepository.existsByEmail(admin.getEmail())) {
                throw new RuntimeException("Email déjà utilisé!");
            }

            String encodedPassword = passwordEncoder.encode(admin.getMotDePasse());
            admin.setMotDePasse(encodedPassword);

            Role adminRole = roleRepository.findByName(ERole.ROLE_ADMIN)
                    .orElseGet(() -> {
                        Role newRole = new Role(ERole.ROLE_ADMIN);
                        return roleRepository.save(newRole);
                    });

            Set<Role> roles = new HashSet<>();
            roles.add(adminRole);
            admin.setRoles(roles);

            return userRepository.save(admin);

        } catch (Exception e) {
            logger.error("Erreur inscription admin: {}", e.getMessage(), e);
            throw new RuntimeException("Erreur lors de l'inscription admin: " + e.getMessage());
        }
    }

    @Transactional
    public boolean deleteUser(Long id) {
        try {
            if (userRepository.existsById(id)) {
                userRepository.deleteById(id);
                logger.info("Utilisateur supprimé avec ID: {}", id);
                return true;
            }
            return false;
        } catch (Exception e) {
            logger.error("Erreur lors de la suppression de l'utilisateur {}: {}", id, e.getMessage());
            throw new RuntimeException("Erreur lors de la suppression de l'utilisateur");
        }
    }

    @Transactional(readOnly = true)
    public List<User> searchUsers(String keyword) {
        return userRepository.findByNomContainingIgnoreCase(keyword);
    }

    @Transactional
    public void changePassword(Long userId, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé avec ID: " + userId));

        // Encoder le nouveau mot de passe
        String encodedPassword = passwordEncoder.encode(newPassword);
        user.setMotDePasse(encodedPassword);

        userRepository.save(user);
        logger.info("Mot de passe changé pour l'utilisateur ID: {}", userId);
    }

    @Transactional(readOnly = true)
    public List<User> findRecentUsers(int limit) {
        return userRepository.findAllNative().stream()
                .sorted((u1, u2) -> u2.getId().compareTo(u1.getId()))
                .limit(limit)
                .collect(Collectors.toList());
    }

    @Transactional
    public void cleanupEmptySiretValues() {
        List<User> allUsers = userRepository.findAll();

        for (User user : allUsers) {
            if (user instanceof Partenaire) {
                Partenaire partenaire = (Partenaire) user;
                boolean needsUpdate = false;

                // Check and clean SIRET
                if (partenaire.getSiret() != null && partenaire.getSiret().trim().isEmpty()) {
                    partenaire.setSiret(null);
                    needsUpdate = true;
                }

                // Check and clean other fields that might have empty strings
                if (partenaire.getSiteWeb() != null && partenaire.getSiteWeb().trim().isEmpty()) {
                    partenaire.setSiteWeb(null);
                    needsUpdate = true;
                }

                if (partenaire.getAdresseEntreprise() != null && partenaire.getAdresseEntreprise().trim().isEmpty()) {
                    partenaire.setAdresseEntreprise(null);
                    needsUpdate = true;
                }

                if (partenaire.getTelephonePro() != null && partenaire.getTelephonePro().trim().isEmpty()) {
                    partenaire.setTelephonePro(null);
                    needsUpdate = true;
                }

                if (needsUpdate) {
                    userRepository.save(partenaire);
                }
            }
        }
    }

    @Override
    public void run(String... args) throws Exception {
        logger.info("Running database cleanup on startup...");
        cleanupEmptySiretValues();
        logger.info("Database cleanup completed.");
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

}