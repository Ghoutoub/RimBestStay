package com.rimbest.rimbest.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import com.Rimbest.rimbest.model.*;
import com.Rimbest.rimbest.repository.ReservationRepository;
import com.Rimbest.rimbest.service.NotificationService;
import com.Rimbest.rimbest.service.ReservationTaskService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

@ExtendWith(MockitoExtension.class)
public class ReservationTaskServiceTest {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private ReservationTaskService reservationTaskService;

    private Reservation reservation;
    private Chambre chambre;
    private Hotel hotel;
    private User client;
    private User partenaire;

    @BeforeEach
    void setUp() {
        partenaire = new User();
        partenaire.setId(1L);
        partenaire.setNom("Partenaire Test");

        hotel = new Hotel();
        hotel.setId(1L);
        hotel.setPartenaire(partenaire);

        chambre = new Chambre();
        chambre.setId(1L);
        chambre.setNumero("101");
        chambre.setHotel(hotel);
        chambre.setDisponible(false);

        client = new User();
        client.setId(2L);
        client.setNom("Client Test");

        reservation = new Reservation();
        reservation.setId(1L);
        reservation.setReference("RES-001");
        reservation.setStatut(StatutReservation.CONFIRMEE);
        reservation.setDateDepart(LocalDate.now().minusDays(1)); // Expired
        reservation.setChambre(chambre);
        reservation.setClient(client);
    }

    @Test
    void testCheckExpiredReservations_ShouldMarkAsTermineeAndNotify() {
        // Arrange
        when(reservationRepository.findByStatutAndDateDepartBefore(eq(StatutReservation.CONFIRMEE),
                any(LocalDate.class)))
                .thenReturn(List.of(reservation));

        // Act
        reservationTaskService.checkExpiredReservations();

        // Assert
        assertEquals(StatutReservation.TERMINEE, reservation.getStatut());
        assertTrue(chambre.getDisponible());

        // Verify client notification
        verify(notificationService).createNotification(
                contains("RES-001"), eq(client), eq(Notification.NotificationType.SUCCESS));

        // Verify partner notification
        verify(notificationService).createNotification(
                contains("RES-001"), eq(partenaire), eq(Notification.NotificationType.INFO));

        verify(reservationRepository).save(reservation);
    }

    @Test
    void testCheckExpiredReservations_NoReservationsFound() {
        // Arrange
        when(reservationRepository.findByStatutAndDateDepartBefore(eq(StatutReservation.CONFIRMEE),
                any(LocalDate.class)))
                .thenReturn(Collections.emptyList());

        // Act
        reservationTaskService.checkExpiredReservations();

        // Assert
        verify(reservationRepository, never()).save(any());
        verify(notificationService, never()).createNotification(anyString(), any(), any());
    }

    private String contains(String s) {
        return org.mockito.ArgumentMatchers.argThat(argument -> argument.contains(s));
    }
}
