package com.Rimbest.rimbest.service;

import com.Rimbest.rimbest.model.*;
import com.Rimbest.rimbest.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.PostConstruct;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReservationTaskService {

    private final ReservationRepository reservationRepository;
    private final NotificationService notificationService;

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void onApplicationReady() {
        log.info("Exécution initiale de la vérification des réservations expirées au démarrage...");
        checkExpiredReservations();
    }

    /**
     * Tâche planifiée pour vérifier les réservations expirées.
     * S'exécute tous les jours à minuit.
     */
    @Scheduled(cron = "0 0 0 * * ?")
    @Transactional
    public void checkExpiredReservations() {
        log.info("Vérification des réservations expirées...");
        LocalDate today = LocalDate.now();

        // Trouver les réservations confirmées dont la date de départ est passée
        List<Reservation> expiredReservations = reservationRepository.findByStatutAndDateDepartBefore(
                StatutReservation.CONFIRMEE, today);

        for (Reservation reservation : expiredReservations) {
            expireReservation(reservation);
        }

        log.info("{} réservations ont été marquées comme terminées.", expiredReservations.size());
    }

    private void expireReservation(Reservation reservation) {
        // Mise à jour du statut uniquement (sans validation)
        reservationRepository.updateStatut(reservation.getId(), StatutReservation.TERMINEE);

        // 2. Rendre la chambre disponible
        Chambre chambre = reservation.getChambre();
        if (chambre != null) {
            chambre.setDisponible(true);
        }

        // 3. Notifier le client
        String clientMsg = String.format(
                "Votre séjour pour la réservation %s est terminé. Nous espérons que vous avez apprécié votre séjour !",
                reservation.getReference());
        notificationService.createNotification(clientMsg, reservation.getClient(),
                Notification.NotificationType.SUCCESS);

        // 4. Notifier le partenaire (ou Admin-créateur)
        User partenaire = (chambre != null && chambre.getHotel() != null) ? chambre.getHotel().getPartenaire() : null;
        if (partenaire != null) {
            String partnerMsg = String.format(
                    "Le séjour du client %s pour la réservation %s est terminé. La chambre %s est maintenant disponible.",
                    reservation.getClient().getNom(), reservation.getReference(), chambre.getNumero());
            notificationService.createNotification(partnerMsg, partenaire, Notification.NotificationType.INFO);
        }

        reservationRepository.save(reservation);
    }
}
