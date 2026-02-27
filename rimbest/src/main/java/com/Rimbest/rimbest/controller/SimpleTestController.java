package com.Rimbest.rimbest.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class SimpleTestController {
    
    // Test très simple - sans dépendances
    @GetMapping("/simple-test")
    public String simpleTest(Model model) {
        model.addAttribute("message", "Test réussi!");
        model.addAttribute("timestamp", new java.util.Date());
        return "test/simple";
    }
    
    // Test avec paramètres
    @GetMapping("/simple-test/search")
    public String simpleSearch(
            @RequestParam(value = "ville", required = false) String ville,
            @RequestParam(value = "dateArrivee", required = false) String dateArrivee,
            Model model) {
        
        model.addAttribute("ville", ville);
        model.addAttribute("dateArrivee", dateArrivee);
        model.addAttribute("message", "Recherche test OK");
        
        return "test/simple-search";
    }
}