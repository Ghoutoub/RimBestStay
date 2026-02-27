package com.Rimbest.rimbest.controller;

import com.Rimbest.rimbest.model.ERole;
import com.Rimbest.rimbest.model.User;
import com.Rimbest.rimbest.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class TestController {
    
    @Autowired
    private UserService userService;
    
    @GetMapping("/test-users")
    public String testUsers(Model model) {
        System.out.println("=== TEST DES UTILISATEURS ===");
        
        // Test 1: findAllUsers()
        System.out.println("\nTest 1: findAllUsers()");
        List<User> users1 = userService.findAllUsers();
        System.out.println("Nombre d'utilisateurs: " + (users1 != null ? users1.size() : "null"));
        
        // Test 2: Méthode alternative
        System.out.println("\nTest 2: findAllUsersAlternative()");
        List<User> users2 = userService.findAllUsersAlternative();
        System.out.println("Nombre d'utilisateurs: " + (users2 != null ? users2.size() : "null"));
        
        // Test 3: Comptage
        System.out.println("\nTest 3: Comptages");
        System.out.println("Total: " + userService.countAllUsers());
        System.out.println("Clients: " + userService.countUsersByRole(ERole.ROLE_CLIENT));
        System.out.println("Partenaires: " + userService.countUsersByRole(ERole.ROLE_PARTENAIRE));
        System.out.println("Admins: " + userService.countUsersByRole(ERole.ROLE_ADMIN));
        
        // Afficher quelques utilisateurs
        if (users2 != null && !users2.isEmpty()) {
            System.out.println("\nLes 5 premiers utilisateurs:");
            for (int i = 0; i < Math.min(5, users2.size()); i++) {
                User user = users2.get(i);
                System.out.println((i+1) + ". " + user.getNom() + " - " + user.getEmail() + 
                    " - Rôles: " + (user.getRoles() != null ? user.getRoles().size() : "null"));
            }
        }
        
        model.addAttribute("users", users2);
        return "test-users";
    }
}