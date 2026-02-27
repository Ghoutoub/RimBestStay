package com.Rimbest.rimbest.controller;

import com.Rimbest.rimbest.model.Reservation;
import com.Rimbest.rimbest.service.PaiementService;
import com.Rimbest.rimbest.service.ReservationService;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/paiements")
@RequiredArgsConstructor
public class PaiementController {
    
    private final PaiementService paiementService;
    private final ReservationService reservationService;
    
    @GetMapping("/reservation/{reservationId}")
    public String paiementReservation(@PathVariable Long reservationId, Model model) {
        model.addAttribute("reservation", reservationService.getReservationById(reservationId));
        model.addAttribute("paiements", paiementService.getPaiementsByReservation(reservationId));
        return "client/paiement/formulaire";
    }
    
    @PostMapping("/process/{reservationId}")
    public String processPaiement(@PathVariable Long reservationId,
                                 @RequestParam String methode,
                                 RedirectAttributes redirectAttributes) {
        
        try {
            // Récupérer la réservation et calculer le montant dû
            var reservation = reservationService.getReservationById(reservationId);
            var paiement = paiementService.createPaiement(reservationId, 
                reservation.getPrixTotal(), methode);
            
            // Pour le moment, simulons un paiement réussi
            paiementService.validerPaiement(paiement.getId());
            
            // Confirmer la réservation
            reservationService.confirmReservation(reservationId);
            
            redirectAttributes.addFlashAttribute("success", 
                "Paiement effectué avec succès ! Référence: " + paiement.getReference());
            return "redirect:/reservations/client/details/" + reservationId;
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur de paiement: " + e.getMessage());
            return "redirect:/paiements/reservation/" + reservationId;
        }
    }


    @PostMapping("/virement/{reservationId}")
public String initierVirement(@PathVariable Long reservationId,
                             Model model) {
    
    Reservation reservation = reservationService.getReservationById(reservationId);
    
    // Générer les infos de virement
    Map<String, String> infosVirement = new HashMap<>();
    infosVirement.put("beneficiaire", "RIMBestStay SARL");
    infosVirement.put("iban", "MA64 0000 0000 0000 0000 0000 123");
    infosVirement.put("swift", "ABCDMAFR");
    infosVirement.put("montant", reservation.getPrixTotal().toString() + " MRU");
    infosVirement.put("reference", reservation.getReference());
    infosVirement.put("delai", "48 heures");
    
    model.addAttribute("infosVirement", infosVirement);
    model.addAttribute("reservation", reservation);
    
    return "client/paiement/virement";
}
}