package com.Rimbest.rimbest.repository;

import com.Rimbest.rimbest.model.ERole;
import com.Rimbest.rimbest.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(ERole name);
}