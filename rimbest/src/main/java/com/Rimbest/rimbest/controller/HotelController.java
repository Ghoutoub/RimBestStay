package com.Rimbest.rimbest.controller;

import com.Rimbest.rimbest.model.Hotel;
import com.Rimbest.rimbest.model.User;
import com.Rimbest.rimbest.service.FileStorageService;
import com.Rimbest.rimbest.service.HotelService;
import com.Rimbest.rimbest.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import jakarta.validation.Valid;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/hotel")
public class HotelController {

    @Autowired
    private HotelService hotelService;

    @Autowired
    private UserService userService;

    @Autowired
    private FileStorageService fileStorageService;

    // ============ LISTE DES HÔTELS ============

    @GetMapping("/list")
    public String listHotels(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "ville", required = false) String ville,
            @RequestParam(value = "etoiles", required = false) Integer etoiles,
            @RequestParam(value = "actif", required = false) Boolean actif,
            Model model) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String role = auth.getAuthorities().toString();
        User currentUser = userService.findByEmail(auth.getName()).orElse(null);

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Hotel> hotelPage;

        if (role.contains("ROLE_PARTENAIRE") && currentUser != null) {
            List<Hotel> myHotels = hotelService.findHotelsByPartenaire(currentUser);
            hotelPage = Page.empty();
            model.addAttribute("hotels", myHotels);
            model.addAttribute("totalItems", myHotels.size());
        } else {
            hotelPage = hotelService.searchHotelsPage(search, ville, etoiles, actif, pageable);
            model.addAttribute("hotels", hotelPage.getContent());
            model.addAttribute("totalItems", hotelPage.getTotalElements());
        }

        if (role.contains("ROLE_ADMIN")) {
            model.addAttribute("totalHotels", hotelService.countAllHotels());
            model.addAttribute("activeHotels", hotelService.getNombreHotelsActifs());
        }

        model.addAttribute("search", search);
        model.addAttribute("ville", ville);
        model.addAttribute("etoiles", etoiles);
        model.addAttribute("actif", actif);

        model.addAttribute("currentPage", page);
        model.addAttribute("pageSize", size);
        model.addAttribute("totalPages", hotelPage.getTotalPages());

        model.addAttribute("isAdmin", role.contains("ROLE_ADMIN"));
        model.addAttribute("isPartenaire", role.contains("ROLE_PARTENAIRE"));
        model.addAttribute("isClient", role.contains("ROLE_CLIENT"));

        return "hotel/list";
    }

    // ============ DÉTAILS D'UN HÔTEL ============

    @GetMapping("/details/{id}")
    public String hotelDetails(@PathVariable("id") Long id, Model model) {
        Optional<Hotel> hotelOpt = hotelService.getHotelByIdWithChambres(id);

        if (hotelOpt.isEmpty()) {
            model.addAttribute("error", "Hôtel non trouvé");
            return "redirect:/hotel/list";
        }

        Hotel hotel = hotelOpt.get();
        model.addAttribute("hotel", hotel);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String role = auth.getAuthorities().toString();
        User currentUser = userService.findByEmail(auth.getName()).orElse(null);

        boolean canEdit = isOwner(hotel, currentUser);
        boolean isAdmin = role.contains("ROLE_ADMIN");

        model.addAttribute("canEdit", canEdit);
        model.addAttribute("isAdmin", isAdmin);
        model.addAttribute("isPartenaire", role.contains("ROLE_PARTENAIRE"));
        model.addAttribute("isClient", role.contains("ROLE_CLIENT"));

        return "hotel/details";
    }

    // ============ AJOUT D'UN HÔTEL ============

    @GetMapping("/add")
    public String showAddForm(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String role = auth.getAuthorities().toString();

        if (!role.contains("ROLE_ADMIN") && !role.contains("ROLE_PARTENAIRE")) {
            return "redirect:/hotel/list";
        }

        Hotel hotel = new Hotel();
        model.addAttribute("hotel", hotel);
        model.addAttribute("isEdit", false);

        return "hotel/add";
    }

    @PostMapping("/add")
    public String addHotel(@Valid @ModelAttribute("hotel") Hotel hotel,
            BindingResult result,
            @RequestParam(value = "imageFiles", required = false) List<MultipartFile> imageFiles,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            model.addAttribute("isEdit", false);
            return "hotel/add";
        }

        try {
            if (imageFiles != null && !imageFiles.isEmpty()) {
                List<String> imageUrls = fileStorageService.storeMultipleFiles(
                        imageFiles,
                        "hotels/" + hotel.getNom().replaceAll("[^a-zA-Z0-9]", "_"));

                hotel.setImagesUrls(String.join(",", imageUrls));
            }

            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            User currentUser = userService.findByEmail(auth.getName()).orElse(null);

            // Tout créateur (Admin ou Partenaire) devient le "partenaire" (propriétaire) de
            // l'hôtel
            if (currentUser != null) {
                hotel.setPartenaire(currentUser);
            }

            Hotel savedHotel = hotelService.createHotel(hotel);
            redirectAttributes.addFlashAttribute("success",
                    "Hôtel " + savedHotel.getNom() + " créé avec succès!");

            return "redirect:/hotel/details/" + savedHotel.getId();

        } catch (Exception e) {
            model.addAttribute("error", "Erreur lors de la création: " + e.getMessage());
            model.addAttribute("isEdit", false);
            return "hotel/add";
        }
    }

    // ============ MODIFICATION D'UN HÔTEL ============

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable("id") Long id, Model model) {
        Optional<Hotel> hotelOpt = hotelService.getHotelById(id);

        if (hotelOpt.isEmpty()) {
            model.addAttribute("error", "Hôtel non trouvé");
            return "redirect:/hotel/list";
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = userService.findByEmail(auth.getName()).orElse(null);
        Hotel hotel = hotelOpt.get();

        if (!isOwner(hotel, currentUser)) {
            return "redirect:/hotel/details/" + id;
        }

        model.addAttribute("hotel", hotel);
        model.addAttribute("isEdit", true);

        return "hotel/edit";
    }

    @PostMapping("/edit/{id}")
    public String updateHotel(@PathVariable("id") Long id,
            @Valid @ModelAttribute("hotel") Hotel hotelDetails,
            BindingResult result,
            @RequestParam(value = "imageFiles", required = false) List<MultipartFile> imageFiles,
            @RequestParam(value = "imagesToRemove", required = false) List<String> imagesToRemove,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            model.addAttribute("isEdit", true);
            return "hotel/edit";
        }

        try {
            Hotel existingHotel = hotelService.getHotelById(id)
                    .orElseThrow(() -> new RuntimeException("Hôtel non trouvé"));

            String updatedImagesUrls = existingHotel.getImagesUrls();
            if (imagesToRemove != null && !imagesToRemove.isEmpty()) {
                if (updatedImagesUrls != null && !updatedImagesUrls.isEmpty()) {
                    List<String> currentImages = new ArrayList<>(Arrays.asList(updatedImagesUrls.split(",")));
                    for (String imageToRemove : imagesToRemove) {
                        currentImages.remove(imageToRemove.trim());
                        try {
                            fileStorageService.deleteFile(imageToRemove);
                        } catch (IOException e) {
                        }
                    }
                    updatedImagesUrls = currentImages.isEmpty() ? null : String.join(",", currentImages);
                }
            }

            if (imageFiles != null && !imageFiles.isEmpty()) {
                List<String> imageUrls = fileStorageService.storeMultipleFiles(
                        imageFiles,
                        "hotels/" + hotelDetails.getNom().replaceAll("[^a-zA-Z0-9]", "_"));

                StringBuilder urlsBuilder = new StringBuilder();
                if (updatedImagesUrls != null && !updatedImagesUrls.isEmpty()) {
                    urlsBuilder.append(updatedImagesUrls);
                }
                for (String url : imageUrls) {
                    if (urlsBuilder.length() > 0)
                        urlsBuilder.append(",");
                    urlsBuilder.append(url);
                }
                hotelDetails.setImagesUrls(urlsBuilder.toString());
            } else {
                hotelDetails.setImagesUrls(updatedImagesUrls);
            }

            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            User currentUser = userService.findByEmail(auth.getName()).orElse(null);
            boolean isAdmin = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
            Hotel updatedHotel = hotelService.updateHotel(id, hotelDetails, currentUser, isAdmin);
            redirectAttributes.addFlashAttribute("success",
                    "Hôtel " + updatedHotel.getNom() + " modifié avec succès!");

            return "redirect:/hotel/details/" + id;

        } catch (Exception e) {
            model.addAttribute("error", "Erreur lors de la modification: " + e.getMessage());
            model.addAttribute("isEdit", true);
            return "hotel/edit";
        }
    }

    // ============ SUPPRESSION D'UN HÔTEL ============

    @PostMapping("/delete/{id}")
    public String deleteHotel(@PathVariable("id") Long id,
            RedirectAttributes redirectAttributes) {

        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            User currentUser = userService.findByEmail(auth.getName()).orElse(null);

            hotelService.deleteHotel(id, currentUser);
            redirectAttributes.addFlashAttribute("success", "Hôtel supprimé avec succès!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    "Erreur lors de la suppression: " + e.getMessage());
        }

        return "redirect:/hotel/list";
    }

    // ============ TOGGLE STATUT ============

    @PostMapping("/toggle-status/{id}")
    public String toggleHotelStatus(@PathVariable("id") Long id,
            RedirectAttributes redirectAttributes) {

        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            User currentUser = userService.findByEmail(auth.getName()).orElse(null);
            boolean isAdmin = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

            hotelService.toggleHotelStatus(id, currentUser, isAdmin);
            redirectAttributes.addFlashAttribute("success", "Statut de l'hôtel changé avec succès!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    "Erreur lors du changement de statut: " + e.getMessage());
        }

        return "redirect:/hotel/list";
    }

    // ============ RECHERCHE RAPIDE ============

    @GetMapping("/search")
    @ResponseBody
    public List<Hotel> searchHotels(@RequestParam("q") String query) {
        return hotelService.searchHotels(query, null, null);
    }

    @GetMapping("/{hotelId}/chambres/add")
    public String redirectHotelToChambreAdd(@PathVariable("hotelId") Long hotelId) {
        return "redirect:/chambre/add/" + hotelId;
    }

    // ============ API POUR STATISTIQUES ============

    @GetMapping("/api/statistics")
    @ResponseBody
    public ResponseEntity<StatisticsDto> getStatistics() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (!auth.getAuthorities().stream().anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        final long totalHotels = hotelService.countAllHotels();
        final long activeHotels = hotelService.getNombreHotelsActifs();

        List<Hotel> hotels = hotelService.getAllHotels();
        final long totalChambres = hotels.stream()
                .mapToLong(h -> h.getChambres() != null ? h.getChambres().size() : 0)
                .sum();
        final long availableChambres = hotels.stream()
                .flatMap(h -> h.getChambres() != null ? h.getChambres().stream() : java.util.stream.Stream.empty())
                .filter(ch -> ch.getDisponible())
                .count();

        return ResponseEntity.ok(new StatisticsDto(totalHotels, activeHotels, totalChambres, availableChambres));
    }

    public static class StatisticsDto {
        public final long totalHotels;
        public final long activeHotels;
        public final long totalChambres;
        public final long availableChambres;

        public StatisticsDto(long totalHotels, long activeHotels, long totalChambres, long availableChambres) {
            this.totalHotels = totalHotels;
            this.activeHotels = activeHotels;
            this.totalChambres = totalChambres;
            this.availableChambres = availableChambres;
        }
    }

    private boolean isOwner(Hotel hotel, User user) {
        if (hotel == null || user == null)
            return false;
        return (hotel.getPartenaire() != null && hotel.getPartenaire().getId().equals(user.getId()));
    }
}