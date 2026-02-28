package com.Rimbest.rimbest.controller;

import com.Rimbest.rimbest.model.Reservation;
import com.Rimbest.rimbest.model.StatutPaiement;
import com.Rimbest.rimbest.model.User;
import com.Rimbest.rimbest.model.dto.PaymentRequestDTO;
import com.Rimbest.rimbest.model.Notification;
import com.Rimbest.rimbest.service.NotificationService;
import com.Rimbest.rimbest.service.PaiementService;
import com.Rimbest.rimbest.service.ReservationService;
import com.Rimbest.rimbest.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/paiements")
@RequiredArgsConstructor
public class PaiementRestController {

    private final PaiementService paiementService;
    private final ReservationService reservationService;
    private final UserService userService;
    private final NotificationService notificationService;

    private User getAuthenticatedUser(Authentication authentication) {
        if (authentication == null)
            return null;
        return userService.findByEmail(authentication.getName()).orElse(null);
    }

    @PostMapping("/process/{reservationId}")
    public ResponseEntity<?> processPaiement(@PathVariable Long reservationId,
            @RequestBody PaymentRequestDTO paymentRequest,
            Authentication authentication) {
        User user = getAuthenticatedUser(authentication);
        if (user == null)
            return ResponseEntity.status(401).body(Map.of("message", "Session expirée. Veuillez vous reconnecter."));

        try {
            Reservation reservation = reservationService.getReservationById(reservationId);

            if (reservation == null) {
                return ResponseEntity.status(404).body(Map.of("message", "La réservation spécifiée est introuvable."));
            }

            if (!reservation.getClient().getId().equals(user.getId())) {
                return ResponseEntity.status(403)
                        .body(Map.of("message", "Action non autorisée sur cette réservation."));
            }

            String methodLabel = "CASH".equalsIgnoreCase(paymentRequest.getMethod()) ? "ESPECES" : "CARTE_BANCAIRE";

            if ("CARD".equalsIgnoreCase(paymentRequest.getMethod())) {
                // Validation simple et sécurisée
                if (paymentRequest.getCardNumber() == null || paymentRequest.getCardNumber().length() < 12) {
                    return ResponseEntity.status(400)
                            .body(Map.of("message", "Données de paiement incomplètes ou invalides."));
                }

                String cardNumber = paymentRequest.getCardNumber().replaceAll("\\s", "");
                if (!"4242424242424242".equals(cardNumber)) {
                    return ResponseEntity.status(402)
                            .body(Map.of("message", "La transaction bancaire a été déclinée par votre banque."));
                }

                // Phase de Notification du Partenaire (Avant confirmation finale)
                User partenaire = reservation.getChambre().getHotel().getPartenaire();
                if (partenaire != null) {
                    String msg = "Nouveau paiement reçu : Réservation #" + reservation.getReference() +
                            " payée par " + user.getNom() + " (" + reservation.getPrixTotal() + " MRU).";
                    notificationService.createNotification(msg, partenaire, Notification.NotificationType.INFO);
                }
            }

            // Création et validation technique du paiement
            var paiement = paiementService.createPaiement(reservationId, reservation.getPrixTotal(), methodLabel);
            paiementService.validerPaiement(paiement.getId());

            // Mise à jour de l'état de la réservation
            reservation.setModePaiement(methodLabel);
            reservation.setStatutPaiement(StatutPaiement.PAYE);

            // Confirmation finale et verrouillage de la chambre
            reservationService.confirmReservation(reservationId);

            return ResponseEntity.ok(Map.of(
                    "message", "Opération réalisée avec succès. Merci de votre confiance.",
                    "reference", paiement.getReference()));

        } catch (Exception e) {
            // Pas de détails sensibles dans le message d'erreur utilisateur
            return ResponseEntity.status(500)
                    .body(Map.of("message", "Un incident technique est survenu. Veuillez réessayer ultérieurement."));
        }
    }
}
