package com.Rimbest.rimbest.controller;

import com.Rimbest.rimbest.model.*;
import com.Rimbest.rimbest.model.dto.*;
import com.Rimbest.rimbest.service.*;
import com.Rimbest.rimbest.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/partenaire")
@PreAuthorize("hasRole('PARTENAIRE')")
public class PartenaireRestController {

    @Autowired
    private UserService userService;

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private HotelRepository hotelRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private ChambreRepository chambreRepository;

    // GET /api/partenaire/hotels?page=&size=&search=
    @GetMapping("/hotels")
    public ResponseEntity<Page<HotelResponse>> getMyHotels(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search) {

        User user = userService.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));

        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());

        // On pourrait ajouter un filtre search ici
        Page<Hotel> hotelPage = hotelRepository.findByPartenaire(user, pageable);

        return ResponseEntity.ok(hotelPage.map(HotelResponse::fromHotel));
    }

    // GET /api/partenaire/hotels/min
    @GetMapping("/hotels/min")
    public ResponseEntity<List<MyHotelMini>> getMyHotelsMini(Authentication authentication) {
        User user = userService.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));

        List<Hotel> hotels = hotelRepository.findByPartenaire(user);
        List<MyHotelMini> miniList = hotels.stream()
                .map(h -> new MyHotelMini(h.getId(), h.getNom()))
                .collect(Collectors.toList());

        return ResponseEntity.ok(miniList);
    }

    // GET /api/partenaire/reservations?page=&size=&statut=&search=
    @GetMapping("/reservations")
    public ResponseEntity<Page<ReservationDTO>> getMyReservations(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String statut,
            @RequestParam(required = false) String search) {

        User user = userService.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));

        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());

        // On utilise findByPartenaire paginé
        Page<Reservation> reservationPage = reservationRepository.findByPartenaire(user, pageable);

        return ResponseEntity.ok(reservationPage.map(reservationService::convertToDTO));
    }

    // PUT /api/partenaire/reservations/{id}/status
    @PutMapping("/reservations/{id}/status")
    public ResponseEntity<ReservationDTO> setReservationStatus(
            @PathVariable Long id,
            @RequestBody StatusUpdateRequest request,
            Authentication authentication) {

        // Vérification que la réservation appartient bien au partenaire
        Reservation reservation = reservationService.getReservationById(id);
        if (reservation == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        if (!reservation.getChambre().getHotel().getPartenaire().getEmail().equals(authentication.getName())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        if (request.getStatut() != null) {
            String status = request.getStatut();
            if ("CONFIRMEE".equals(status)) {
                reservationService.confirmReservation(id);
            } else {
                reservationService.refuseReservation(id, "Refusée par le partenaire");
            }
        } else {
            if (request.isActivate()) {
                reservationService.confirmReservation(id);
            } else {
                reservationService.refuseReservation(id, "Refusée par le partenaire");
            }
        }

        return ResponseEntity.ok(reservationService.convertToDTO(reservationService.getReservationById(id)));
    }

    // GET /api/partenaire/chambres?page=&size=&hotelId=&search=
    @GetMapping("/chambres")
    public ResponseEntity<Page<ChambreResponse>> getMyChambres(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long hotelId,
            @RequestParam(required = false) String search) {

        User user = userService.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));

        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());

        // Simplifié : on récupère toutes les chambres des hôtels du partenaire
        // Pour être plus précis on devrait filtrer par hotelId si fourni
        Page<Chambre> chambrePage;
        if (hotelId != null) {
            chambrePage = chambreRepository.findByHotelId(hotelId, pageable);
        } else {
            chambrePage = chambreRepository.findByPartenaire(user, pageable);
        }

        return ResponseEntity.ok(chambrePage.map(ChambreResponse::fromChambre));
    }

    // DTOs internes ou importés
    public static class MyHotelMini {
        private Long id;
        private String nom;

        public MyHotelMini(Long id, String nom) {
            this.id = id;
            this.nom = nom;
        }

        public Long getId() {
            return id;
        }

        public String getNom() {
            return nom;
        }
    }
}
