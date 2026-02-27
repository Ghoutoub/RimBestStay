package com.Rimbest.rimbest.controller;

import com.Rimbest.rimbest.model.Hotel;
import com.Rimbest.rimbest.model.User;
import com.Rimbest.rimbest.model.dto.HotelRequest;
import com.Rimbest.rimbest.model.dto.HotelResponse;
import com.Rimbest.rimbest.service.FileStorageService;
import com.Rimbest.rimbest.service.HotelService;
import com.Rimbest.rimbest.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/hotels")
public class HotelRestController {

    @Autowired
    private HotelService hotelService;

    @Autowired
    private UserService userService;

    @Autowired
    private FileStorageService fileStorageService;

    @GetMapping
    public ResponseEntity<Page<HotelResponse>> getAllHotels(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String ville,
            @RequestParam(required = false) Integer etoiles,
            @RequestParam(required = false) Boolean actif) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());

        if (authentication != null
                && authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_PARTENAIRE"))) {
            User currentUser = userService.findByEmail(authentication.getName()).orElseThrow();
            List<Hotel> partenaireHotels = hotelService.findHotelsByPartenaire(currentUser);

            if (search != null) {
                partenaireHotels = partenaireHotels.stream()
                        .filter(h -> h.getNom().toLowerCase().contains(search.toLowerCase())).toList();
            }
            if (ville != null) {
                partenaireHotels = partenaireHotels.stream()
                        .filter(h -> h.getVille() != null && h.getVille().equalsIgnoreCase(ville)).toList();
            }
            int start = Math.min((int) pageable.getOffset(), partenaireHotels.size());
            int end = Math.min((start + pageable.getPageSize()), partenaireHotels.size());
            List<HotelResponse> responses = partenaireHotels.subList(start, end).stream().map(HotelResponse::fromHotel)
                    .toList();
            return ResponseEntity
                    .ok(new org.springframework.data.domain.PageImpl<>(responses, pageable, partenaireHotels.size()));
        }

        Page<Hotel> hotelPage = hotelService.searchHotelsPage(search, ville, etoiles, actif, pageable);
        Page<HotelResponse> responsePage = hotelPage.map(HotelResponse::fromHotel);
        return ResponseEntity.ok(responsePage);
    }

    @GetMapping("/search")
    public ResponseEntity<List<HotelResponse>> searchHotels(@RequestParam(name = "q") String query) {
        List<Hotel> hotels = hotelService.searchHotels(query, null, null);
        List<HotelResponse> responses = hotels.stream().map(HotelResponse::fromHotel).toList();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/available")
    public ResponseEntity<Page<HotelResponse>> getAvailableHotels(
            @RequestParam(required = false) String ville,
            @RequestParam(required = false) String pays,
            @RequestParam(required = false) Integer etoilesMin,
            @RequestParam(required = false) Integer etoilesMax,
            @RequestParam(required = false) Double prixMin,
            @RequestParam(required = false) Double prixMax,
            @RequestParam(required = false) String equipement,
            @RequestParam(defaultValue = "1") Integer capacite,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("noteMoyenne").descending());
        Page<Hotel> hotelPage = hotelService.getAvailableHotels(ville, pays, etoilesMin, etoilesMax, prixMin, prixMax,
                capacite, equipement, pageable);
        return ResponseEntity.ok(hotelPage.map(HotelResponse::fromHotel));
    }

    @GetMapping("/{id}")
    public ResponseEntity<HotelResponse> getHotelById(@PathVariable Long id) {
        Hotel hotel = hotelService.getHotelById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Hôtel non trouvé"));
        return ResponseEntity.ok(HotelResponse.fromHotel(hotel));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_PARTENAIRE')")
    public ResponseEntity<HotelResponse> createHotel(
            @RequestPart("hotel") @Valid HotelRequest request,
            @RequestPart(value = "imageFiles", required = false) List<MultipartFile> files) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = userService.findByEmail(auth.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));

        Hotel hotel = new Hotel();
        mapToEntity(request, hotel);

        if (files != null && !files.isEmpty()) {
            try {
                List<String> savedPaths = fileStorageService.storeMultipleFiles(files, "hotels");
                hotel.setImagesUrls(String.join(",", savedPaths));
            } catch (Exception e) {
            }
        }

        // any creator is the owner
        hotel.setPartenaire(currentUser);

        Hotel saved = hotelService.createHotel(hotel);
        return new ResponseEntity<>(HotelResponse.fromHotel(saved), HttpStatus.CREATED);
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_PARTENAIRE')")
    public ResponseEntity<HotelResponse> updateHotel(
            @PathVariable Long id,
            @RequestPart("hotel") @Valid HotelRequest request,
            @RequestPart(value = "imageFiles", required = false) List<MultipartFile> files) {
        Hotel existing = hotelService.getHotelById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Hôtel non trouvé"));

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = userService.findByEmail(auth.getName()).orElseThrow();

        boolean isAdmin = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        // Seul le créateur peut modifier l'hôtel, ou un administrateur
        if (!isOwner(existing, currentUser) && !isAdmin) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Vous n'avez pas l'autorisation de modifier cet hôtel.");
        }

        mapToEntity(request, existing);

        if (files != null && !files.isEmpty()) {
            try {
                List<String> savedPaths = fileStorageService.storeMultipleFiles(files, "hotels");
                String currentUrls = existing.getImagesUrls();
                if (currentUrls != null && !currentUrls.isEmpty()) {
                    existing.setImagesUrls(currentUrls + "," + String.join(",", savedPaths));
                } else {
                    existing.setImagesUrls(String.join(",", savedPaths));
                }
            } catch (Exception e) {
                // Log error
            }
        }

        Hotel updated = hotelService.updateHotel(id, existing, currentUser, isAdmin);
        return ResponseEntity.ok(HotelResponse.fromHotel(updated));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_PARTENAIRE')")
    public ResponseEntity<Void> deleteHotel(@PathVariable Long id) {
        Hotel existing = hotelService.getHotelById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Hôtel non trouvé"));

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = userService.findByEmail(auth.getName()).orElseThrow();

        boolean isAdmin = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        // Seul le créateur peut supprimer l'hôtel, ou un administrateur
        if (!isOwner(existing, currentUser) && !isAdmin) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Vous n'avez pas l'autorisation de supprimer cet hôtel.");
        }

        hotelService.deleteHotel(id, currentUser);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/toggle-status")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_PARTENAIRE')")
    public ResponseEntity<HotelResponse> toggleStatus(@PathVariable Long id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = userService.findByEmail(auth.getName()).orElseThrow();

        Hotel hotel = hotelService.getHotelById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Hôtel non trouvé"));

        boolean isAdmin = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        boolean isOwner = isOwner(hotel, currentUser);

        // L'admin peut changer le statut de tous les hôtels, le partenaire seulement
        // les siens
        if (!isAdmin && !isOwner) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Vous n'avez pas la permission de changer le statut de cet hôtel.");
        }

        hotelService.toggleHotelStatus(id, currentUser, isAdmin);
        return ResponseEntity.ok(HotelResponse.fromHotel(hotelService.getHotelById(id).get()));
    }

    private void mapToEntity(HotelRequest request, Hotel hotel) {
        if (org.springframework.util.StringUtils.hasText(request.getNom()))
            hotel.setNom(request.getNom());
        if (org.springframework.util.StringUtils.hasText(request.getAdresse()))
            hotel.setAdresse(request.getAdresse());
        if (org.springframework.util.StringUtils.hasText(request.getVille()))
            hotel.setVille(request.getVille());
        if (org.springframework.util.StringUtils.hasText(request.getPays()))
            hotel.setPays(request.getPays());
        if (org.springframework.util.StringUtils.hasText(request.getDescription()))
            hotel.setDescription(request.getDescription());
        if (org.springframework.util.StringUtils.hasText(request.getTelephone()))
            hotel.setTelephone(request.getTelephone());
        if (org.springframework.util.StringUtils.hasText(request.getEmail()))
            hotel.setEmail(request.getEmail());
        if (org.springframework.util.StringUtils.hasText(request.getEquipementsHotel()))
            hotel.setEquipementsHotel(request.getEquipementsHotel());

        if (request.getEtoiles() != null)
            hotel.setEtoiles(request.getEtoiles());
        if (request.getActif() != null)
            hotel.setActif(request.getActif());

        if (request.getImagesUrls() != null) {
            hotel.setImagesUrls(String.join(",", request.getImagesUrls()));
        }
    }

    private boolean isOwner(Hotel hotel, User user) {
        if (hotel == null || user == null)
            return false;
        return (hotel.getPartenaire() != null && hotel.getPartenaire().getId().equals(user.getId()));
    }
}