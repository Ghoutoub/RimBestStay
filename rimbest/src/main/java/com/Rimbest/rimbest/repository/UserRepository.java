package com.Rimbest.rimbest.repository;

import com.Rimbest.rimbest.model.Role;
import com.Rimbest.rimbest.model.User;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    Boolean existsByEmail(String email);

    // NOUVELLES MÉTHODES
    List<User> findByRolesContaining(Role role);

    @Query("SELECT COUNT(u) FROM User u JOIN u.roles r WHERE r = :role")
    long countByRolesContaining(@Param("role") Role role);

    List<User> findByActifTrue();

    List<User> findByNomContainingIgnoreCase(String nom);

    Page<User> findByNomContainingIgnoreCaseOrEmailContainingIgnoreCase(String nom, String email, Pageable pageable);

    @Query("SELECT DISTINCT u FROM User u LEFT JOIN FETCH u.roles")
    List<User> findAllNative();

    @Query("SELECT DISTINCT u FROM User u LEFT JOIN FETCH u.roles")
    List<User> findAllWithRolesNative();
}