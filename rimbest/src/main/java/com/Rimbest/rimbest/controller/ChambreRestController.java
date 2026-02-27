package com.Rimbest.rimbest.controller;

import com.Rimbest.rimbest.model.Chambre;
import com.Rimbest.rimbest.model.Hotel;
import com.Rimbest.rimbest.model.dto.ChambreRequest;
import com.Rimbest.rimbest.model.dto.ChambreResponse;
import com.Rimbest.rimbest.service.ChambreService;
import com.Rimbest.rimbest.service.HotelService;
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
import com.Rimbest.rimbest.model.User;
import com.Rimbest.rimbest.service.UserService;
import com.Rimbest.rimbest.service.FileStorageService;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@RestController
@RequestMapping("/api/chambres")
public class ChambreRestController {

    @Autowired
    private ChambreService chambreService;

    @Autowired
    private HotelService hotelService;

    @Autowired
    private UserService userService;

    @Autowired
    private FileStorageService fileStorageService;

    // GET /api/chambres (admin)
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<ChambreResponse>> getAllChambres(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<Chambre> chambrePage = chambreService.searchChambresPage(null, null, null, null, null, null, pageable);
        return ResponseEntity.ok(chambrePage.map(ChambreResponse::fromChambre));
    }

    // GET /api/chambres/{id}
    @GetMapping("/{id}")
    public ResponseEntity<ChambreResponse> getChambreById(@PathVariable Long id) {
        Chambre chambre = chambreService.getChambreById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Chambre non trouvée"));
        return ResponseEntity.ok(ChambreResponse.fromChambre(chambre));
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_PARTENAIRE')")
    public ResponseEntity<ChambreResponse> createChambre(Authentication authentication,
            @Valid @RequestBody ChambreRequest request) {
        if (request.getHotelId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "hotelId est requis");
        }

        Hotel hotel = hotelService.getHotelById(request.getHotelId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Hôtel non trouvé"));

        User currentUser = userService.findByEmail(authentication.getName()).orElseThrow();
        if (!isOwner(hotel, currentUser)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Vous n'êtes pas le créateur/propriétaire de cet hôtel");
        }

        Chambre chambre = new Chambre();
        mapToEntity(request, chambre);
        chambre.setHotel(hotel);

        Chambre saved = chambreService.createChambre(chambre, currentUser);
        return new ResponseEntity<>(ChambreResponse.fromChambre(saved), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_PARTENAIRE')")
    public ResponseEntity<ChambreResponse> updateChambre(
            Authentication authentication,
            @PathVariable Long id,
            @Valid @RequestBody ChambreRequest request) {
        Chambre existing = chambreService.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Chambre non trouvée"));

        User currentUser = userService.findByEmail(authentication.getName()).orElseThrow();
        if (!isOwner(existing.getHotel(), currentUser)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Vous n'êtes pas le créateur/propriétaire de cet hôtel");
        }

        mapToEntity(request, existing);

        if (request.getHotelId() != null) {
            Hotel hotel = hotelService.getHotelById(request.getHotelId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Hôtel non trouvé"));

            // Si on change d'hôtel, on vérifie aussi la propriété du nouvel hôtel
            if (!isOwner(hotel, currentUser)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "Vous n'êtes pas le créateur/propriétaire du nouvel hôtel");
            }
            existing.setHotel(hotel);
        }

        Chambre updated = chambreService.updateChambre(id, existing, currentUser);
        return ResponseEntity.ok(ChambreResponse.fromChambre(updated));
    }

    @PutMapping(value = "/{id}/multipart", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_PARTENAIRE')")
    public ResponseEntity<ChambreResponse> updateChambreMultipart(
            Authentication authentication,
            @PathVariable Long id,
            @RequestPart("chambre") @Valid ChambreRequest request,
            @RequestPart(value = "imageFiles", required = false) List<MultipartFile> files) {

        Chambre existing = chambreService.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Chambre non trouvée"));

        User currentUser = userService.findByEmail(authentication.getName()).orElseThrow();
        if (!isOwner(existing.getHotel(), currentUser)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Vous n'êtes pas le créateur/propriétaire de cet hôtel");
        }

        mapToEntity(request, existing);

        // Nouveaux fichiers
        if (files != null && !files.isEmpty()) {
            try {
                List<String> savedPaths = fileStorageService.storeMultipleFiles(files, "chambres");
                String currentUrls = existing.getImagesChambre();
                existing.setImagesChambre(
                        (currentUrls == null || currentUrls.isEmpty())
                                ? String.join(",", savedPaths)
                                : currentUrls + "," + String.join(",", savedPaths));
            } catch (Exception e) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Erreur stockage images");
            }
        }

        Chambre updated = chambreService.updateChambre(id, existing, currentUser);
        return ResponseEntity.ok(ChambreResponse.fromChambre(updated));
    }

    private void mapToEntity(ChambreRequest request, Chambre chambre) {
        // Chaînes : hasText() protège contre null ET chaîne vide ""
        if (org.springframework.util.StringUtils.hasText(request.getNumero()))
            chambre.setNumero(request.getNumero());
        if (org.springframework.util.StringUtils.hasText(request.getType()))
            chambre.setTypeChambre(request.getType());
        if (org.springframework.util.StringUtils.hasText(request.getDescription()))
            chambre.setDescription(request.getDescription());
        if (org.springframework.util.StringUtils.hasText(request.getEquipementsChambre()))
            chambre.setEquipementsChambre(request.getEquipementsChambre());
        if (org.springframework.util.StringUtils.hasText(request.getVueType()))
            chambre.setVueType(request.getVueType());
        if (org.springframework.util.StringUtils.hasText(request.getTypeLits()))
            chambre.setTypeLits(request.getTypeLits());

        // Numériques : null = non fourni
        if (request.getCapacite() != null)
            chambre.setCapacite(request.getCapacite());
        if (request.getPrixParNuit() != null)
            chambre.setPrixNuit(request.getPrixParNuit());
        if (request.getPrixWeekend() != null)
            chambre.setPrixWeekend(request.getPrixWeekend());
        if (request.getDisponible() != null)
            chambre.setDisponible(request.getDisponible());
        if (request.getSuperficie() != null)
            chambre.setSuperficie(request.getSuperficie());
        if (request.getNombreLits() != null)
            chambre.setNombreLits(request.getNombreLits());
        if (request.getTaxeSejour() != null)
            chambre.setTaxeSejour(request.getTaxeSejour());
        if (request.getDepotGarantie() != null)
            chambre.setDepotGarantie(request.getDepotGarantie());
        if (request.getSalleBainPrivee() != null)
            chambre.setSalleBainPrivee(request.getSalleBainPrivee());
        if (request.getClimatisation() != null)
            chambre.setClimatisation(request.getClimatisation());
        if (request.getTelevision() != null)
            chambre.setTelevision(request.getTelevision());
        if (request.getWifi() != null)
            chambre.setWifi(request.getWifi());
        if (request.getMinibar() != null)
            chambre.setMinibar(request.getMinibar());
        if (request.getCoffreFort() != null)
            chambre.setCoffreFort(request.getCoffreFort());
        if (org.springframework.util.StringUtils.hasText(request.getStatutNettoyage()))
            chambre.setStatutNettoyage(request.getStatutNettoyage());

        // Images existantes à conserver
        if (request.getImagesUrls() != null) {
            chambre.setImagesChambre(String.join(",", request.getImagesUrls()));
        }
    }

    // DELETE /api/chambres/{id}
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_PARTENAIRE')")
    public ResponseEntity<Void> deleteChambre(Authentication authentication, @PathVariable Long id) {
        Chambre existing = chambreService.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Chambre non trouvée"));

        User currentUser = userService.findByEmail(authentication.getName()).orElseThrow();
        if (!isOwner(existing.getHotel(), currentUser)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Vous n'êtes pas le créateur/propriétaire de cet hôtel");
        }

        chambreService.deleteChambre(id, currentUser);
        return ResponseEntity.noContent().build();
    }

    // GET /api/chambres/hotel/{hotelId}
    @GetMapping("/hotel/{hotelId}")
    public ResponseEntity<Page<ChambreResponse>> getChambresByHotel(
            @PathVariable Long hotelId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Boolean disponible) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<Chambre> chambrePage = chambreService.searchChambresPage(hotelId, search, type, null, null, disponible,
                pageable);
        return ResponseEntity.ok(chambrePage.map(ChambreResponse::fromChambre));
    }

    @PostMapping("/hotel/{hotelId}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_PARTENAIRE')")
    public ResponseEntity<ChambreResponse> createChambreForHotel(
            Authentication authentication,
            @PathVariable Long hotelId,
            @Valid @RequestBody ChambreRequest request) {
        request.setHotelId(hotelId);
        return createChambre(authentication, request);
    }

    @PostMapping(value = "/hotel/{hotelId}/multipart", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_PARTENAIRE')")
    public ResponseEntity<ChambreResponse> createChambreForHotelMultipart(
            Authentication authentication,
            @PathVariable Long hotelId,
            @RequestPart("chambre") @Valid ChambreRequest request,
            @RequestPart(value = "imageFiles", required = false) List<MultipartFile> files) {

        Hotel hotel = hotelService.getHotelById(hotelId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Hôtel non trouvé"));

        User currentUser = userService.findByEmail(authentication.getName()).orElseThrow();
        if (!isOwner(hotel, currentUser)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Vous n'êtes pas le créateur/propriétaire de cet hôtel");
        }

        Chambre chambre = new Chambre();
        chambre.setHotel(hotel);
        mapToEntity(request, chambre);

        if (files != null && !files.isEmpty()) {
            try {
                List<String> savedPaths = fileStorageService.storeMultipleFiles(files, "chambres");
                chambre.setImagesChambre(String.join(",", savedPaths));
            } catch (Exception e) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Erreur stockage images");
            }
        }

        Chambre saved = chambreService.createChambre(chambre, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(ChambreResponse.fromChambre(saved));
    }

    // GET /api/chambres/hotel/{hotelId}/disponibles
    @GetMapping("/hotel/{hotelId}/disponibles")
    public ResponseEntity<List<ChambreResponse>> getAvailableChambres(
            @PathVariable Long hotelId,
            @RequestParam(required = false) Integer capacite) {
        Hotel hotel = hotelService.getHotelById(hotelId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Hôtel non trouvé"));
        // Simplifié : on filtre par hôtel et dispo
        List<Chambre> chambres = chambreService.findByHotel(hotel).stream()
                .filter(c -> c.getDisponible() && (capacite == null || c.getCapacite() >= capacite))
                .toList();
        return ResponseEntity.ok(chambres.stream().map(ChambreResponse::fromChambre).toList());
    }

    private boolean isOwner(Hotel hotel, User user) {
        if (hotel == null || user == null)
            return false;
        return (hotel.getPartenaire() != null && hotel.getPartenaire().getId().equals(user.getId()));
    }
}
