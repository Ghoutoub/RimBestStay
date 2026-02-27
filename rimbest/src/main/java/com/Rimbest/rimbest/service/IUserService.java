package com.Rimbest.rimbest.service;

import com.Rimbest.rimbest.model.*;
import java.util.List;
import java.util.Optional;

public interface IUserService {
    User registerClient(Client client);
    Optional<User> findByEmail(String email);
    User registerPartenaire(Partenaire partenaire);
    User registerAdministrator(Administrator admin);
    List<User> findAllUsers();
    List<User> findUsersByRole(ERole role);
    Optional<User> findById(Long id);
    User updateUser(Long id, User userDetails);
    void deactivateUser(Long id);
    void activateUser(Long id);
    User changeUserRole(Long userId, ERole newRole);
    boolean emailExists(String email);
    long countAllUsers();
    long countUsersByRole(ERole role);
    Optional<User> getCurrentUser(String email);
    void createTestUsers();
    boolean deleteUser(Long id);
    List<User> searchUsers(String keyword);
}