package com.Rimbest.rimbest.service;

import com.Rimbest.rimbest.model.Hotel;
import com.Rimbest.rimbest.model.User;
import com.Rimbest.rimbest.repository.HotelRepository;

import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class HotelService {

    @Autowired
    private HotelRepository hotelRepository;

    // ============ MÉTHODES CRUD ============

    @Transactional(readOnly = true)
    public List<Hotel> getAllHotels() {
        return hotelRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Hotel> getHotelById(Long id) {
        return hotelRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<Hotel> getHotelByIdWithChambres(Long id) {
        return hotelRepository.findByIdWithChambres(id);
    }

    @Transactional
    public Hotel createHotel(Hotel hotel) {
        hotel.setCreatedAt(LocalDateTime.now());
        hotel.setUpdatedAt(LocalDateTime.now());
        if (hotel.getActif() == null) {
            hotel.setActif(true);
        }
        return hotelRepository.save(hotel);
    }

    @Transactional
    public Hotel updateHotel(Long id, Hotel hotelDetails, User currentUser ,boolean isAdmin) {
        Hotel hotel = hotelRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Hôtel non trouvé avec l'ID: " + id));

        // Permission check: Only owner (Partner or Admin who created it) can update
        if (!isOwner(hotel, currentUser)) {
            throw new RuntimeException("Vous n'avez pas la permission de modifier cet hôtel.");
        }

        hotel.setNom(hotelDetails.getNom());
        hotel.setVille(hotelDetails.getVille());
        hotel.setPays(hotelDetails.getPays());
        hotel.setEtoiles(hotelDetails.getEtoiles());
        hotel.setDescription(hotelDetails.getDescription());
        hotel.setAdresse(hotelDetails.getAdresse());
        hotel.setTelephone(hotelDetails.getTelephone());
        hotel.setEmail(hotelDetails.getEmail());
        hotel.setActif(hotelDetails.getActif());
        hotel.setUpdatedAt(LocalDateTime.now());

        if (hotelDetails.getImagesUrls() != null) {
            hotel.setImagesUrls(hotelDetails.getImagesUrls());
        }

        // Only allow changing owner if the current user is an admin?
        // For now, let's keep it simple.
        if (hotelDetails.getPartenaire() != null && isAdmin) {
            hotel.setPartenaire(hotelDetails.getPartenaire());
        }

        hotelRepository.save(hotel);

        Hotel refreshed = hotelRepository.findByIdWithChambres(id)
                .orElseThrow(() -> new RuntimeException("Erreur lors du rechargement de l'hôtel"));

        if (refreshed.getPartenaire() != null) {
            Hibernate.initialize(refreshed.getPartenaire());
        }

        return refreshed;
    }

    @Transactional
    public void deleteHotel(Long id, User currentUser) {
        Hotel hotel = hotelRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Hôtel non trouvé avec l'ID: " + id));

        // Permission check: Only owner can delete
        if (!isOwner(hotel, currentUser)) {
            throw new RuntimeException("Vous n'avez pas la permission de supprimer cet hôtel.");
        }

        hotelRepository.delete(hotel);
    }

    // ============ MÉTHODES DE RECHERCHE ============

    @Transactional(readOnly = true)
    public List<Hotel> searchHotels(String keyword, String ville, Integer etoiles, Boolean actif) {
        return hotelRepository.searchHotels(keyword, ville, etoiles, actif);
    }

    @Transactional(readOnly = true)
    public List<Hotel> searchHotels(String keyword, String ville, Integer etoiles) {
        return hotelRepository.searchHotels(keyword, ville, etoiles, null);
    }

    @Transactional(readOnly = true)
    public Page<Hotel> searchHotelsPage(String keyword, String ville, Integer etoiles, Boolean actif,
            Pageable pageable) {
        return hotelRepository.searchHotelsPage(keyword, ville, etoiles, actif, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Hotel> getAvailableHotels(String ville, String pays, Integer etoilesMin, Integer etoilesMax,
            Double prixMin, Double prixMax, Integer capacite, String equipement, Pageable pageable) {
        return hotelRepository.findAvailableHotels(ville, pays, etoilesMin, etoilesMax, prixMin, prixMax, capacite,
                equipement, pageable);
    }

    // ============ MÉTHODES STATISTIQUES ============

    @Transactional(readOnly = true)
    public long countAllHotels() {
        return hotelRepository.count();
    }

    @Transactional(readOnly = true)
    public long getNombreHotelsActifs() {
        return hotelRepository.countByActifTrue();
    }

    @Transactional(readOnly = true)
    public List<Hotel> findRecentHotels(int limit) {
        return hotelRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(0, limit));
    }

    @Transactional(readOnly = true)
    public List<Hotel> findHotelsByPartenaire(User partenaire) {
        return hotelRepository.findByPartenaire(partenaire);
    }

    @Transactional(readOnly = true)
    public long countChambresByPartenaire(User partenaire) {
        List<Hotel> hotels = hotelRepository.findByPartenaire(partenaire);
        return hotels.stream()
                .mapToLong(hotel -> hotel.getChambres() != null ? hotel.getChambres().size() : 0)
                .sum();
    }

    @Transactional(readOnly = true)
    public long countAvailableChambresByPartenaire(User partenaire) {
        List<Hotel> hotels = hotelRepository.findByPartenaire(partenaire);
        return hotels.stream()
                .flatMap(hotel -> hotel.getChambres() != null ? hotel.getChambres().stream()
                        : java.util.stream.Stream.empty())
                .filter(chambre -> chambre.getDisponible())
                .count();
    }

    @Transactional(readOnly = true)
    public List<Hotel> findAllActiveHotels() {
        return hotelRepository.findByActifTrue();
    }

    @Transactional(readOnly = true)
    public List<Hotel> findHotelsByVille(String ville) {
        return hotelRepository.findByVilleContainingIgnoreCase(ville);
    }

    @Transactional(readOnly = true)
    public List<Hotel> findHotelsByPays(String pays) {
        return hotelRepository.findByPays(pays);
    }

    @Transactional(readOnly = true)
    public List<Hotel> findHotelsByEtoiles(Integer etoiles) {
        return hotelRepository.findByEtoiles(etoiles);
    }

    @Transactional(readOnly = true)
    public boolean existsByNomAndVille(String nom, String ville) {
        return hotelRepository.existsByNomAndVille(nom, ville);
    }

    // ============ MÉTHODES PAGINATION ============

    @Transactional(readOnly = true)
    public Page<Hotel> getAllHotels(Pageable pageable) {
        return hotelRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Page<Hotel> getActiveHotels(Pageable pageable) {
        return hotelRepository.findByActifTrue(pageable);
    }

    // ============ MÉTHODES UTILITAIRES ============

    @Transactional
    public void toggleHotelStatus(Long id, User currentUser, boolean isAdmin) {
        Hotel hotel = hotelRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Hôtel non trouvé avec l'ID: " + id));

        // Permission check: Owner OR any Admin can toggle status
        if (!isOwner(hotel, currentUser) && !isAdmin) {
            throw new RuntimeException("Vous n'avez pas la permission de changer l'état de cet hôtel.");
        }

        hotel.setActif(!hotel.getActif());
        hotel.setUpdatedAt(LocalDateTime.now());
        hotelRepository.save(hotel);
    }

    private boolean isOwner(Hotel hotel, User user) {
        return hotel.getPartenaire() != null && hotel.getPartenaire().getId().equals(user.getId());
    }

    @Transactional(readOnly = true)
    public List<String> getAllVilles() {
        return hotelRepository.findAllVillesDistinct();
    }

    @Transactional(readOnly = true)
    public List<String> getAllPays() {
        return hotelRepository.findAllPaysDistinct();
    }

}