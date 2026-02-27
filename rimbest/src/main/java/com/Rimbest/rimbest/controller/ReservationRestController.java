package com.Rimbest.rimbest.controller;

import com.Rimbest.rimbest.model.Reservation;
import com.Rimbest.rimbest.model.User;
import com.Rimbest.rimbest.model.dto.ReservationDTO;
import com.Rimbest.rimbest.model.dto.ReservationRequestDTO;
import com.Rimbest.rimbest.model.dto.ReservationStatsDTO;
import com.Rimbest.rimbest.model.dto.StatusUpdateRequest;
import com.Rimbest.rimbest.service.ReservationService;
import com.Rimbest.rimbest.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/reservations")
public class ReservationRestController {

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private UserService userService;

    // GET /api/reservations (admin/partenaire)
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PARTENAIRE')")
    public ResponseEntity<Page<ReservationDTO>> getAllReservations(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());

        if (authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            return ResponseEntity
                    .ok(reservationService.getReservationsPage(pageable).map(reservationService::convertToDTO));
        } else {
            User user = userService.findByEmail(authentication.getName()).orElseThrow();
            // Pour le partenaire, on pourrait avoir besoin d'une méthode paginée spécifique
            // ou filtrer
            List<Reservation> list = reservationService.getReservationsByPartenaire(user);
            // Pagination manuelle simplifiée pour l'exemple, idéalement en repository
            int start = Math.min((int) pageable.getOffset(), list.size());
            int end = Math.min((start + pageable.getPageSize()), list.size());
            List<Reservation> subList = list.subList(start, end);
            List<ReservationDTO> dtoPageList = reservationService.convertToDTOList(subList);
            return ResponseEntity
                    .ok(new org.springframework.data.domain.PageImpl<>(dtoPageList, pageable, list.size()));
        }
    }

    // GET /api/reservations/client (client)
    @GetMapping("/client")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<List<ReservationDTO>> getClientReservations(Authentication authentication) {
        User user = userService.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
        List<Reservation> list = reservationService.getReservationsByClient(user);
        return ResponseEntity.ok(reservationService.convertToDTOList(list));
    }

    // GET /api/reservations/{id}
    @GetMapping("/{id}")
    public ResponseEntity<ReservationDTO> getReservationById(@PathVariable Long id) {
        Reservation reservation = reservationService.getReservationById(id);
        if (reservation == null)
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        return ResponseEntity.ok(reservationService.convertToDTO(reservation));
    }

    // POST /api/reservations
    @PostMapping
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<ReservationDTO> createReservation(
            Authentication authentication,
            @Valid @RequestBody ReservationRequestDTO request) {
        User user = userService.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));

        Reservation reservation = reservationService.createReservation(request, user);
        return new ResponseEntity<>(reservationService.convertToDTO(reservation), HttpStatus.CREATED);
    }

    // PUT /api/reservations/{id}/statut
    @PutMapping("/{id}/statut")
    @PreAuthorize("hasAnyRole('ADMIN', 'PARTENAIRE')")
    public ResponseEntity<ReservationDTO> updateStatus(
            @PathVariable Long id,
            @RequestBody StatusUpdateRequest request) {
        // Logique de confirmation ou refus basée sur le booléen activate de
        // StatusUpdateRequest
        Reservation reservation;
        if (request.isActivate()) {
            reservation = reservationService.confirmReservation(id);
        } else {
            reservation = reservationService.refuseReservation(id, "Annulée via l'API");
        }
        return ResponseEntity.ok(reservationService.convertToDTO(reservation));
    }

    @GetMapping("/stats")
    @PreAuthorize("hasAnyRole('ADMIN', 'PARTENAIRE')")
    public ResponseEntity<ReservationStatsDTO> getStats(Authentication authentication) {
        if (authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            return ResponseEntity.ok(reservationService.getStats());
        } else {
            User user = userService.findByEmail(authentication.getName())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
            return ResponseEntity.ok(reservationService.getStats(user));
        }
    }
}
