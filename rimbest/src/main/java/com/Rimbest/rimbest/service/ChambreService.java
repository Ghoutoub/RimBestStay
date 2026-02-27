package com.Rimbest.rimbest.service;

import com.Rimbest.rimbest.model.Chambre;
import com.Rimbest.rimbest.model.Hotel;
import com.Rimbest.rimbest.model.User;
import com.Rimbest.rimbest.repository.ChambreRepository;
import com.Rimbest.rimbest.service.ReservationService;

import org.springframework.context.annotation.Lazy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class ChambreService {

    @Autowired
    // @Lazy
    private ChambreRepository chambreRepository;
    @Autowired
    @Lazy
    private ReservationService reservationService;

    // ============ MÉTHODES CRUD ============

    @Transactional(readOnly = true)
    public List<Chambre> getAllChambres() {
        return chambreRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Chambre> getChambreById(Long id) {
        return chambreRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<Chambre> findById(Long id) {
        return getChambreById(id);
    }

    @Transactional
    public Chambre save(Chambre chambre) {
        chambre.setUpdatedAt(LocalDateTime.now());
        if (chambre.getCreatedAt() == null) {
            chambre.setCreatedAt(LocalDateTime.now());
        }
        return chambreRepository.save(chambre);
    }

    @Transactional
    public Chambre createChambre(Chambre chambre, User currentUser) {
        // Permission check: Only hotel owner can create rooms
        if (!isHotelOwner(chambre.getHotel(), currentUser)) {
            throw new RuntimeException("Vous n'avez pas la permission d'ajouter une chambre à cet hôtel.");
        }

        chambre.setCreatedAt(LocalDateTime.now());
        chambre.setUpdatedAt(LocalDateTime.now());
        if (Objects.isNull(chambre.getDisponible())) {
            chambre.setDisponible(true);
        }
        if (chambre.getStatutNettoyage() == null) {
            chambre.setStatutNettoyage("PROPRE");
        }
        return chambreRepository.save(chambre);
    }

    @Transactional
    public Chambre updateChambre(Long id, Chambre chambreDetails, User currentUser) {
        Chambre chambre = chambreRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Chambre non trouvée avec l'ID: " + id));

        // Permission check: Only hotel owner can update rooms
        if (!isHotelOwner(chambre.getHotel(), currentUser)) {
            throw new RuntimeException("Vous n'avez pas la permission de modifier cette chambre.");
        }

        chambre.setNumero(chambreDetails.getNumero());
        chambre.setTypeChambre(chambreDetails.getTypeChambre());
        chambre.setCapacite(chambreDetails.getCapacite());
        chambre.setPrixNuit(chambreDetails.getPrixNuit());
        chambre.setPrixWeekend(chambreDetails.getPrixWeekend());
        chambre.setDescription(chambreDetails.getDescription());
        chambre.setSuperficie(chambreDetails.getSuperficie());
        chambre.setTaxeSejour(chambreDetails.getTaxeSejour());
        chambre.setDepotGarantie(chambreDetails.getDepotGarantie());
        chambre.setEquipementsChambre(chambreDetails.getEquipementsChambre());
        chambre.setDisponible(chambreDetails.getDisponible());
        chambre.setStatutNettoyage(chambreDetails.getStatutNettoyage());
        chambre.setUpdatedAt(LocalDateTime.now());

        if (chambreDetails.getImagesChambre() != null) {
            chambre.setImagesChambre(chambreDetails.getImagesChambre());
        }

        return chambreRepository.save(chambre);
    }

    @Transactional
    public void deleteChambre(Long id, User currentUser) {
        Chambre chambre = chambreRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Chambre non trouvée avec l'ID: " + id));

        // Permission check: Only hotel owner can delete rooms
        if (!isHotelOwner(chambre.getHotel(), currentUser)) {
            throw new RuntimeException("Vous n'avez pas la permission de supprimer cette chambre.");
        }

        chambreRepository.delete(chambre);
    }

    // ============ MÉTHODES DE RECHERCHE ============

    @Transactional(readOnly = true)
    public List<Chambre> findByHotel(Hotel hotel) {
        return chambreRepository.findByHotel(hotel);
    }

    @Transactional(readOnly = true)
    public Page<Chambre> searchChambresPage(Long hotelId, String search, String typeChambre,
            Double minPrice, Double maxPrice, Boolean disponible,
            Pageable pageable) {
        return chambreRepository.searchChambresPage(hotelId, search, typeChambre,
                minPrice, maxPrice, disponible, pageable);
    }

    @Transactional(readOnly = true)
    public List<Chambre> searchChambres(Long hotelId, String search, String typeChambre,
            Double minPrice, Double maxPrice, Boolean disponible) {
        return chambreRepository.searchChambres(hotelId, search, typeChambre,
                minPrice, maxPrice, disponible);
    }

    @Transactional(readOnly = true)
    public List<Chambre> findAvailableChambresByHotel(Hotel hotel) {
        return chambreRepository.findByHotelAndDisponibleTrue(hotel);
    }

    @Transactional(readOnly = true)
    public List<Chambre> findChambresByType(String type) {
        return chambreRepository.findByTypeChambre(type);
    }

    // ============ MÉTHODES STATISTIQUES ============

    @Transactional(readOnly = true)
    public long countByHotel(Hotel hotel) {
        return chambreRepository.countByHotel(hotel);
    }

    @Transactional(readOnly = true)
    public long countAvailableByHotel(Hotel hotel) {
        return chambreRepository.countByHotelAndDisponibleTrue(hotel);
    }

    @Transactional(readOnly = true)
    public double getAveragePriceByHotel(Hotel hotel) {
        Double avg = chambreRepository.findAveragePriceByHotel(hotel);
        return avg != null ? avg : 0.0;
    }

    @Transactional(readOnly = true)
    public double getMinPriceByHotel(Hotel hotel) {
        Double min = chambreRepository.findMinPriceByHotel(hotel);
        return min != null ? min : 0.0;
    }

    @Transactional(readOnly = true)
    public double getMaxPriceByHotel(Hotel hotel) {
        Double max = chambreRepository.findMaxPriceByHotel(hotel);
        return max != null ? max : 0.0;
    }

    // ============ MÉTHODES DE VÉRIFICATION ============

    @Transactional(readOnly = true)
    public boolean existsByHotelAndNumero(Hotel hotel, String numero) {
        return chambreRepository.existsByHotelAndNumero(hotel, numero);
    }

    // ============ MÉTHODES UTILITAIRES ============

    @Transactional
    public Chambre toggleDisponibilite(Long id, User currentUser) {
        Chambre chambre = chambreRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Chambre non trouvée"));

        // Permission check: Owner OR any Admin can toggle room availability
        if (!isHotelOwner(chambre.getHotel(), currentUser) && !isAdmin(currentUser)) {
            throw new RuntimeException("Vous n'avez pas la permission de changer la disponibilité de cette chambre.");
        }

        chambre.setDisponible(!chambre.getDisponible());
        chambre.setUpdatedAt(LocalDateTime.now());

        return chambreRepository.save(chambre);
    }

    private boolean isHotelOwner(Hotel hotel, User user) {
        if (hotel == null || user == null)
            return false;
        return hotel.getPartenaire() != null && hotel.getPartenaire().getId().equals(user.getId());
    }

    private boolean isAdmin(User user) {
        if (user == null || user.getRoles() == null)
            return false;
        return user.getRoles().stream()
                .anyMatch(role -> role.getName() == com.Rimbest.rimbest.model.ERole.ROLE_ADMIN);
    }

    @Transactional
    public void updateNettoyageStatus(Long id, String statut) {
        Chambre chambre = chambreRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Chambre non trouvée avec l'ID: " + id));

        chambre.setStatutNettoyage(statut);
        chambre.setUpdatedAt(LocalDateTime.now());
        chambreRepository.save(chambre);
    }

    @Transactional(readOnly = true)
    public List<String> getAllTypesChambre() {
        return chambreRepository.findAllTypesChambreDistinct();
    }

    // Ajouter à la fin de la classe

    @Transactional(readOnly = true)
    public boolean isChambreDisponible(Long chambreId, LocalDate dateArrivee, LocalDate dateDepart) {
        // Appeler le service de réservation
        // Cette méthode sera implémentée dans le service de réservation
        return true; // temporaire
    }

    @Transactional(readOnly = true)
    public BigDecimal calculerPrixSejour(Long chambreId, LocalDate dateArrivee, LocalDate dateDepart,
            Integer personnes) {
        // Appeler le service de réservation
        // Cette méthode sera implémentée dans le service de réservation
        return BigDecimal.ZERO; // temporaire
    }

    @Transactional(readOnly = true)
    public boolean isChambreDisponiblePourAujourdhui(Long chambreId) {
        LocalDate aujourdhui = LocalDate.now();
        LocalDate demain = aujourdhui.plusDays(1);

        // Vérifier si la chambre est en service
        Optional<Chambre> chambreOpt = getChambreById(chambreId);
        if (chambreOpt.isEmpty() || !chambreOpt.get().getDisponible()) {
            return false;
        }

        // Vérifier les réservations
        return reservationService.checkDisponibilite(chambreId, aujourdhui, demain);
    }

    @Transactional(readOnly = true)
    public boolean isChambreDisponiblePourPeriode(Long chambreId, LocalDate dateDebut, LocalDate dateFin) {
        Optional<Chambre> chambreOpt = getChambreById(chambreId);
        if (chambreOpt.isEmpty() || !chambreOpt.get().getDisponible()) {
            return false;
        }

        return reservationService.checkDisponibilite(chambreId, dateDebut, dateFin);
    }

}