package com.Rimbest.rimbest.controller;

import com.Rimbest.rimbest.repository.UserRepository;
import com.Rimbest.rimbest.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DebugController {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private UserService userService;
    
    @GetMapping("/debug")
    public String debugPage(Model model) {
        // Compter les utilisateurs
        long userCount = userRepository.count();
        model.addAttribute("userCount", userCount);
        
        // Lister les utilisateurs
        model.addAttribute("users", userRepository.findAll());
        
        return "debug";
    }
}