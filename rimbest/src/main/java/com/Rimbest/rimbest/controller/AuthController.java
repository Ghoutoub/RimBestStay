package com.Rimbest.rimbest.controller;

import com.Rimbest.rimbest.model.Client;
import com.Rimbest.rimbest.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class AuthController {
    
    @Autowired
    private UserService userService;
    
    @GetMapping("/login")
    public String login(Model model) {
        return "auth/login";
    }
    
    @GetMapping("/inscription")
    public String showRegistrationForm(Model model) {
        model.addAttribute("client", new Client());
        return "auth/inscription";
    }
    
    @PostMapping("/inscription")
    public String registerClient(@ModelAttribute Client client, Model model) {
        try {
            userService.registerClient(client);
            model.addAttribute("success", "Inscription réussie! Vous pouvez maintenant vous connecter.");
            return "auth/login";
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("client", client);
            return "auth/inscription";
        }
    }
}