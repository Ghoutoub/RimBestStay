package com.Rimbest.rimbest.service.impl;

import com.Rimbest.rimbest.model.dto.ReservationDTO;
import com.Rimbest.rimbest.model.dto.ReservationRequestDTO;
import com.Rimbest.rimbest.model.dto.DisponibiliteDTO;
import com.Rimbest.rimbest.model.dto.ReservationStatsDTO;
import com.Rimbest.rimbest.model.*;
import com.Rimbest.rimbest.repository.ChambreRepository;
import com.Rimbest.rimbest.repository.ReservationRepository;
import com.Rimbest.rimbest.service.ChambreService;
import com.Rimbest.rimbest.service.ReservationService;
import com.Rimbest.rimbest.service.UserService;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReservationServiceImpl implements ReservationService {

    private final ReservationRepository reservationRepository;
    private final ChambreRepository chambreRepository;
    private final ChambreService chambreService;
    private final UserService userService;

    @Override
    @Transactional
    public Reservation createReservation(ReservationRequestDTO requestDTO, User client) {
        // 1. Vérifier disponibilité
        if (!checkDisponibilite(requestDTO.getChambreId(),
                requestDTO.getDateDebut(), requestDTO.getDateFin())) {
            throw new RuntimeException("La chambre n'est pas disponible pour ces dates");
        }

        // 2. Récupérer chambre
        Chambre chambre = chambreRepository.findById(requestDTO.getChambreId())
                .orElseThrow(() -> new RuntimeException("Chambre non trouvée"));

        // 3. Calculer prix
        BigDecimal prixTotal = calculatePrixTotal(
                requestDTO.getChambreId(),
                requestDTO.getDateDebut(),
                requestDTO.getDateFin(),
                requestDTO.getNbPersonnes());

        // 4. Créer réservation
        Reservation reservation = new Reservation();
        reservation.setClient(client);
        reservation.setChambre(chambre);
        reservation.setDateArrivee(requestDTO.getDateDebut());
        reservation.setDateDepart(requestDTO.getDateFin());
        reservation.setNombrePersonnes(requestDTO.getNbPersonnes());
        reservation.setPrixTotal(prixTotal);
        reservation.setStatut(StatutReservation.EN_ATTENTE);

        // Définir le mode de paiement par défaut
        reservation.setModePaiement("Cash");
        reservation.setStatutPaiement(StatutPaiement.EN_ATTENTE);

        // 5. Sauvegarder
        return reservationRepository.save(reservation);
    }

    @Override
    @Transactional
    public Reservation confirmReservation(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("Réservation non trouvée"));

        // Vérifier que la chambre est toujours disponible
        if (!checkDisponibilite(reservation.getChambre().getId(),
                reservation.getDateArrivee(), reservation.getDateDepart(), reservation.getId())) {
            throw new RuntimeException("La chambre n'est plus disponible");
        }

        reservation.setStatut(StatutReservation.CONFIRMEE);

        // Mettre à jour la disponibilité de la chambre
        updateChambreDisponibilite(reservation.getChambre(), false);

        return reservationRepository.save(reservation);
    }

    /**
     * Met à jour la disponibilité d'une chambre
     * 
     * @param chambre    La chambre à mettre à jour
     * @param disponible true si disponible, false si occupée
     */
    private void updateChambreDisponibilite(Chambre chambre, boolean disponible) {
        chambre.setDisponible(disponible);
        chambre.setUpdatedAt(LocalDateTime.now());
        // Note: La chambre est automatiquement sauvegardée avec la réservation
    }

    @Override
    @Transactional
    public Reservation cancelReservation(Long reservationId, String raison) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("Réservation non trouvée"));

        // Remettre la chambre disponible
        if (reservation.getStatut() == StatutReservation.CONFIRMEE) {
            updateChambreDisponibilite(reservation.getChambre(), true);
        }

        reservation.setStatut(StatutReservation.ANNULEE);
        return reservationRepository.save(reservation);
    }

    @Override
    @Transactional
    public Reservation refuseReservation(Long reservationId, String raison) {
        System.out.println("DEBUG: refuseReservation called with id: " + reservationId + ", raison: " + raison);
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("Réservation non trouvée"));
        System.out.println("DEBUG: Reservation found with status: " + reservation.getStatut());

        // Refuser la réservation
        reservation.setStatut(StatutReservation.ANNULEE);
        System.out.println("DEBUG: Status set to ANNULEE");

        // Optionnel: Enregistrer la raison du refus
        System.out.println("DEBUG: Réservation " + reservation.getReference() +
                " refusée. Raison: " + raison);

        Reservation savedReservation = reservationRepository.save(reservation);
        System.out.println("DEBUG: Reservation sauvegardée avec status: " + savedReservation.getStatut());

        return savedReservation;
    }

    @Transactional
    public Reservation updateReservationStatus(Long reservationId, StatutReservation nouveauStatut, String raison) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("Réservation non trouvée"));

        // Vérifier la transition valide
        validateStatusTransition(reservation.getStatut(), nouveauStatut);

        // Mettre à jour le statut
        reservation.setStatut(nouveauStatut);

        // Ajouter un log ou historique si nécessaire
        System.out.println("DEBUG: Réservation " + reservation.getReference() +
                " - Statut: " + reservation.getStatut() + " -> " + nouveauStatut +
                " - Raison: " + raison);

        return reservationRepository.save(reservation);
    }

    private void validateStatusTransition(StatutReservation ancien, StatutReservation nouveau) {
        // Logique de validation des transitions
        if (ancien == StatutReservation.TERMINEE) {
            throw new RuntimeException("Impossible de modifier une réservation terminée");
        }

        if (ancien == StatutReservation.ANNULEE && nouveau != StatutReservation.ANNULEE) {
            throw new RuntimeException("Impossible de réactiver une réservation annulée");
        }
    }

    @Override
    public boolean checkDisponibilite(Long chambreId, LocalDate dateArrivee, LocalDate dateDepart) {
        System.out.println("DEBUG: checkDisponibilite called with chambreId: " + chambreId + ", dateArrivee: "
                + dateArrivee + ", dateDepart: " + dateDepart);
        // Vérifier que la chambre existe et est disponible
        Optional<Chambre> chambreOpt = chambreRepository.findById(chambreId);
        if (chambreOpt.isEmpty() || !chambreOpt.get().getDisponible()) {
            System.out.println("DEBUG: Room not found or not available");
            return false;
        }

        // Vérifier qu'il n'y a pas de réservation en conflit
        boolean isReserved = reservationRepository.isChambreReservee(chambreId, dateArrivee, dateDepart);
        System.out.println("DEBUG: Room is reserved: " + isReserved);
        return !isReserved;
    }

    // Surcharge de la méthode pour exclure une réservation spécifique (utile pour
    // la confirmation)
    public boolean checkDisponibilite(Long chambreId, LocalDate dateArrivee, LocalDate dateDepart,
            Long reservationIdToExclude) {
        System.out.println("DEBUG: checkDisponibilite called with chambreId: " + chambreId + ", dateArrivee: "
                + dateArrivee + ", dateDepart: " + dateDepart + ", excluding reservationId: " + reservationIdToExclude);
        // Vérifier que la chambre existe et est disponible
        Optional<Chambre> chambreOpt = chambreRepository.findById(chambreId);
        if (chambreOpt.isEmpty() || !chambreOpt.get().getDisponible()) {
            System.out.println("DEBUG: Room not found or not available");
            return false;
        }

        // Vérifier qu'il n'y a pas de réservation en conflit (à part celle qu'on
        // exclut)
        // Pour cela, on devra modifier la requête dans le repository ou filtrer
        // manuellement
        List<Reservation> reservationsConflictuelles = reservationRepository.findByChambreAndDateRange(
                chambreId, dateArrivee, dateDepart);

        boolean isReserved = reservationsConflictuelles.stream()
                .anyMatch(r -> !r.getId().equals(reservationIdToExclude) &&
                        (r.getStatut() == StatutReservation.CONFIRMEE
                                || r.getStatut() == StatutReservation.EN_ATTENTE));

        System.out.println("DEBUG: Room is reserved (excluding current): " + isReserved);
        return !isReserved;
    }

    @Override
    public BigDecimal calculatePrixTotal(Long chambreId, LocalDate dateArrivee,
            LocalDate dateDepart, Integer personnes) {
        Chambre chambre = chambreRepository.findById(chambreId)
                .orElseThrow(() -> new RuntimeException("Chambre non trouvée"));

        long nombreNuits = ChronoUnit.DAYS.between(dateArrivee, dateDepart);

        // Calculer le prix en fonction des jours (weekend vs semaine)
        BigDecimal total = BigDecimal.ZERO;
        LocalDate date = dateArrivee;

        while (date.isBefore(dateDepart)) {
            // Vérifier si c'est un week-end (samedi ou dimanche)
            boolean isWeekend = date.getDayOfWeek().getValue() >= 6;
            BigDecimal prixNuit = isWeekend && chambre.getPrixWeekend() != null
                    ? BigDecimal.valueOf(chambre.getPrixWeekend())
                    : BigDecimal.valueOf(chambre.getPrixNuit());

            total = total.add(prixNuit);
            date = date.plusDays(1);
        }

        // Ajouter taxes si applicable
        if (chambre.getTaxeSejour() != null) {
            total = total.add(BigDecimal.valueOf(chambre.getTaxeSejour()));
        }

        return total;
    }

    @Override
    public List<DisponibiliteDTO> getDisponibilitesHotel(Long hotelId, LocalDate dateDebut, LocalDate dateFin) {
        // Récupérer toutes les chambres de l'hôtel
        List<Chambre> chambres = chambreRepository.findByHotelId(hotelId);
        List<DisponibiliteDTO> disponibilites = new ArrayList<>();

        for (Chambre chambre : chambres) {
            DisponibiliteDTO dto = new DisponibiliteDTO();
            dto.setChambreId(chambre.getId());
            dto.setChambreNumero(chambre.getNumero());
            dto.setTypeChambre(chambre.getTypeChambre());
            dto.setCapacite(chambre.getCapacite());
            dto.setPrixNuit(BigDecimal.valueOf(chambre.getPrixNuit()));

            // Récupérer les dates déjà réservées pour cette chambre
            List<Reservation> reservations = reservationRepository.findByChambre(chambre);
            List<LocalDate> datesReservees = new ArrayList<>();

            for (Reservation r : reservations) {
                if (r.getStatut() == StatutReservation.CONFIRMEE ||
                        r.getStatut() == StatutReservation.EN_ATTENTE) {
                    LocalDate date = r.getDateArrivee();
                    while (date.isBefore(r.getDateDepart())) {
                        datesReservees.add(date);
                        date = date.plusDays(1);
                    }
                }
            }

            dto.setDatesReservees(datesReservees);
            dto.setDisponible(chambre.getDisponible() &&
                    checkDisponibilite(chambre.getId(), dateDebut, dateFin));

            disponibilites.add(dto);
        }

        return disponibilites;
    }

    @Override
    public ReservationDTO convertToDTO(Reservation reservation) {
        ReservationDTO dto = new ReservationDTO();
        dto.setId(reservation.getId());
        dto.setReference(reservation.getReference());
        dto.setDateDebut(reservation.getDateArrivee());
        dto.setDateFin(reservation.getDateDepart());
        dto.setNbPersonnes(reservation.getNombrePersonnes());
        dto.setPrixTotal(reservation.getPrixTotal());
        dto.setStatut(reservation.getStatut());

        // Ajouter ces champs manquants
        dto.setDepotGarantie(reservation.getDepotGarantie());
        dto.setReduction(reservation.getReduction());
        dto.setModePaiement(reservation.getModePaiement());
        dto.setStatutPaiement(reservation.getStatutPaiement());

        // Informations client
        if (reservation.getClient() != null) {
            dto.setClientId(reservation.getClient().getId());
            dto.setClientEmail(reservation.getClient().getEmail());
            dto.setClientNom(reservation.getClient().getNom());

            // Check if client is actually a Client instance before calling getTelephone()
            if (reservation.getClient() instanceof Client) {
                dto.setClientTelephone(((Client) reservation.getClient()).getTelephone());
            } else {
                dto.setClientTelephone(null);
            }
        }

        // Informations chambre/hôtel
        if (reservation.getChambre() != null) {
            dto.setChambreId(reservation.getChambre().getId());
            dto.setChambreNumero(reservation.getChambre().getNumero());
            dto.setChambreType(reservation.getChambre().getTypeChambre());

            if (reservation.getChambre().getHotel() != null) {
                dto.setHotelId(reservation.getChambre().getHotel().getId());
                dto.setHotelNom(reservation.getChambre().getHotel().getNom());
            }
        }

        return dto;
    }

    @Override
    public List<ReservationDTO> convertToDTOList(List<Reservation> reservations) {
        return reservations.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // Implémentation des autres méthodes...
    @Override
    public Reservation getReservationById(Long id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Réservation non trouvée"));
    }

    @Override
    public List<Reservation> getReservationsByClient(User client) {
        return reservationRepository.findByClient(client);
    }

    @Override
    public List<Reservation> getReservationsByHotel(Long hotelId) {
        return reservationRepository.findByHotelId(hotelId);
    }

    @Override
    public List<Reservation> getReservationsByPartenaire(User partenaire) {
        return reservationRepository.findByPartenaire(partenaire);
    }

    @Override
    public Page<Reservation> getReservationsPage(Pageable pageable) {
        return reservationRepository.findAll(pageable);
    }

    @Override
    public double getTauxOccupationHotel(Long hotelId, LocalDate startDate, LocalDate endDate) {
        // Récupérer le nombre total de chambres dans l'hôtel
        long totalChambres = chambreService.countByHotel(
                chambreRepository.findById(hotelId)
                        .orElseThrow(() -> new RuntimeException("Hôtel non trouvé"))
                        .getHotel());

        // Récupérer le nombre de chambres occupées
        Integer chambresOccupees = reservationRepository.countChambresOccupeesByHotelInPeriod(
                hotelId, startDate, endDate);

        if (totalChambres == 0)
            return 0.0;

        return (chambresOccupees.doubleValue() / totalChambres) * 100.0;
    }

    @Override
    public Reservation getReservationByReference(String reference) {
        return reservationRepository.findByReference(reference)
                .orElseThrow(() -> new RuntimeException("Réservation non trouvée"));
    }

    @Override
    public long countReservationsByStatut(StatutReservation statut) {
        return reservationRepository.findByStatut(statut).size();
    }

    @Override
    public long countReservationsByHotelAndStatut(Long hotelId, StatutReservation statut) {
        return reservationRepository.countByHotelAndStatut(hotelId, statut);
    }

    @Override
    public BigDecimal getRevenueByHotelAndMonth(Long hotelId, int year, int month) {
        return reservationRepository.getRevenueByHotelAndMonth(hotelId, year, month);
    }

    @Override
    public List<DisponibiliteDTO> getDisponibilitesChambre(Long chambreId, LocalDate dateDebut, LocalDate dateFin) {
        List<DisponibiliteDTO> disponibilites = new ArrayList<>();

        Chambre chambre = chambreRepository.findById(chambreId)
                .orElseThrow(() -> new RuntimeException("Chambre non trouvée"));

        DisponibiliteDTO dto = new DisponibiliteDTO();
        dto.setChambreId(chambre.getId());
        dto.setChambreNumero(chambre.getNumero());
        dto.setTypeChambre(chambre.getTypeChambre());
        dto.setCapacite(chambre.getCapacite());
        dto.setPrixNuit(BigDecimal.valueOf(chambre.getPrixNuit()));

        // Récupérer les réservations de la chambre
        List<Reservation> reservations = reservationRepository.findByChambre(chambre);
        List<LocalDate> datesReservees = new ArrayList<>();

        for (Reservation r : reservations) {
            if (r.getStatut() == StatutReservation.CONFIRMEE ||
                    r.getStatut() == StatutReservation.EN_ATTENTE) {
                LocalDate date = r.getDateArrivee();
                while (date.isBefore(r.getDateDepart())) {
                    datesReservees.add(date);
                    date = date.plusDays(1);
                }
            }
        }

        dto.setDatesReservees(datesReservees);
        dto.setDisponible(checkDisponibilite(chambre.getId(), dateDebut, dateFin));

        disponibilites.add(dto);

        return disponibilites;
    }

    @Override
    public List<Reservation> searchReservations(String keyword, LocalDate dateDebut, LocalDate dateFin,
            StatutReservation statut) {
        // Implementation de recherche basique - peut être améliorée selon les besoins
        List<Reservation> allReservations = reservationRepository.findAll();

        return allReservations.stream()
                .filter(r -> {
                    boolean matchesKeyword = keyword == null || keyword.isEmpty() ||
                            r.getReference().toLowerCase().contains(keyword.toLowerCase()) ||
                            r.getClient().getNom().toLowerCase().contains(keyword.toLowerCase());

                    boolean matchesDates = (dateDebut == null && dateFin == null) ||
                            (r.getDateArrivee().isAfter(dateDebut) || r.getDateArrivee().isEqual(dateDebut)) &&
                                    (r.getDateDepart().isBefore(dateFin) || r.getDateDepart().isEqual(dateFin));

                    boolean matchesStatut = statut == null || r.getStatut() == statut;

                    return matchesKeyword && matchesDates && matchesStatut;
                })
                .collect(Collectors.toList());
    }

    @Override
    public Reservation updateReservation(Long reservationId, ReservationRequestDTO requestDTO) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("Réservation non trouvée"));

        // Vérifier si la mise à jour est possible (pas encore confirmée ou annulée)
        if (reservation.getStatut() == StatutReservation.CONFIRMEE ||
                reservation.getStatut() == StatutReservation.ANNULEE) {
            throw new RuntimeException("Impossible de modifier une réservation " + reservation.getStatut());
        }

        // Vérifier la disponibilité avec les nouvelles dates
        if (!reservation.getDateArrivee().equals(requestDTO.getDateDebut()) ||
                !reservation.getDateDepart().equals(requestDTO.getDateFin())) {
            if (!checkDisponibilite(requestDTO.getChambreId(), requestDTO.getDateDebut(),
                    requestDTO.getDateFin())) {
                throw new RuntimeException("La chambre n'est pas disponible pour les nouvelles dates");
            }
        }

        // Mettre à jour les champs
        Chambre nouvelleChambre = chambreRepository.findById(requestDTO.getChambreId())
                .orElseThrow(() -> new RuntimeException("Chambre non trouvée"));

        reservation.setChambre(nouvelleChambre);
        reservation.setDateArrivee(requestDTO.getDateDebut());
        reservation.setDateDepart(requestDTO.getDateFin());
        reservation.setNombrePersonnes(requestDTO.getNbPersonnes());

        // Recalculer le prix total
        BigDecimal nouveauPrix = calculatePrixTotal(
                requestDTO.getChambreId(),
                requestDTO.getDateDebut(),
                requestDTO.getDateFin(),
                requestDTO.getNbPersonnes());
        reservation.setPrixTotal(nouveauPrix);

        return reservationRepository.save(reservation);
    }

    @Override
    public Page<Reservation> getReservationsByClient(Long clientId, Pageable pageable) {
        return reservationRepository.findByClientId(clientId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public long countTotalReservations() {
        return reservationRepository.count();
    }

    @Override
    @Transactional(readOnly = true)
    public long countReservationsToday() {
        LocalDate today = LocalDate.now();
        return reservationRepository.findAll().stream()
                .filter(r -> r.getCreatedAt() != null &&
                        LocalDate.ofInstant(r.getCreatedAt().toInstant(), java.time.ZoneId.systemDefault())
                                .equals(today))
                .count();
    }

    @Override
    @Transactional(readOnly = true)
    public long countReservationsByClient(User client) {
        return reservationRepository.findAll().stream()
                .filter(r -> r.getClient() != null && r.getClient().getId().equals(client.getId()))
                .count();
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getRevenueForCurrentMonth() {
        LocalDate now = LocalDate.now();
        int year = now.getYear();
        int month = now.getMonthValue();

        return reservationRepository.findAll().stream()
                .filter(r -> r.getStatut() == StatutReservation.CONFIRMEE)
                .filter(r -> r.getCreatedAt() != null)
                .filter(r -> {
                    LocalDate createdAt = LocalDate.ofInstant(r.getCreatedAt().toInstant(),
                            java.time.ZoneId.systemDefault());
                    return createdAt.getYear() == year && createdAt.getMonthValue() == month;
                })
                .map(Reservation::getPrixTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getRevenueByClient(User client) {
        return reservationRepository.findAll().stream()
                .filter(r -> r.getClient() != null && r.getClient().getId().equals(client.getId()))
                .filter(r -> r.getStatut() == StatutReservation.CONFIRMEE)
                .map(Reservation::getPrixTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Reservation> getRecentReservationsByClient(User client, int limit) {
        return reservationRepository.findAll().stream()
                .filter(r -> r.getClient() != null && r.getClient().getId().equals(client.getId()))
                .sorted((r1, r2) -> r2.getCreatedAt().compareTo(r1.getCreatedAt()))
                .limit(limit)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteReservation(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("Réservation non trouvée"));

        // Vérifier si la suppression est autorisée
        if (reservation.getStatut() == StatutReservation.CONFIRMEE) {
            throw new RuntimeException("Impossible de supprimer une réservation confirmée");
        }

        reservationRepository.deleteById(reservationId);
    }

    @Override
    @Transactional(readOnly = true)
    public ReservationStatsDTO getStats() {
        List<Reservation> all = reservationRepository.findAll();

        long enAttente = all.stream().filter(r -> r.getStatut() == StatutReservation.EN_ATTENTE).count();
        long confirmees = all.stream().filter(r -> r.getStatut() == StatutReservation.CONFIRMEE).count();
        long annulees = all.stream().filter(r -> r.getStatut() == StatutReservation.ANNULEE).count();
        long total = all.size();
        long refusees = 0; // Not used in backend

        BigDecimal revenusTotal = all.stream()
                .filter(r -> r.getStatut() == StatutReservation.CONFIRMEE)
                .map(Reservation::getPrixTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Top Hotels
        List<ReservationStatsDTO.TopHotelDTO> topHotels = all.stream()
                .filter(r -> r.getChambre() != null && r.getChambre().getHotel() != null)
                .collect(Collectors.groupingBy(r -> r.getChambre().getHotel().getNom(), Collectors.counting()))
                .entrySet().stream()
                .map(e -> new ReservationStatsDTO.TopHotelDTO(e.getKey(), e.getValue()))
                .sorted((t1, t2) -> Long.compare(t2.getCount(), t1.getCount()))
                .limit(5)
                .collect(Collectors.toList());

        // Monthly Stats (Simplified)
        List<ReservationStatsDTO.MonthlyStatsDTO> monthly = all.stream()
                .filter(r -> r.getCreatedAt() != null)
                .collect(Collectors.groupingBy(r -> {
                    LocalDate d = LocalDate.ofInstant(r.getCreatedAt().toInstant(), java.time.ZoneId.systemDefault());
                    return d.getMonth().name();
                }, Collectors.counting()))
                .entrySet().stream()
                .map(e -> new ReservationStatsDTO.MonthlyStatsDTO(e.getKey(), e.getValue()))
                .collect(Collectors.toList());

        return ReservationStatsDTO.builder()
                .total(total)
                .enAttente(enAttente)
                .confirmees(confirmees)
                .refusees(refusees)
                .annulees(annulees)
                .revenusTotal(revenusTotal)
                .topHotels(topHotels)
                .monthly(monthly)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ReservationStatsDTO getStats(User partenaire) {
        List<Reservation> all = reservationRepository.findByPartenaire(partenaire);

        long enAttente = all.stream().filter(r -> r.getStatut() == StatutReservation.EN_ATTENTE).count();
        long confirmees = all.stream().filter(r -> r.getStatut() == StatutReservation.CONFIRMEE).count();
        long annulees = all.stream().filter(r -> r.getStatut() == StatutReservation.ANNULEE).count();
        long total = all.size();
        long refusees = 0;

        BigDecimal revenusTotal = all.stream()
                .filter(r -> r.getStatut() == StatutReservation.CONFIRMEE)
                .map(Reservation::getPrixTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Top Hotels (pour ce partenaire seulement)
        List<ReservationStatsDTO.TopHotelDTO> topHotels = all.stream()
                .filter(r -> r.getChambre() != null && r.getChambre().getHotel() != null)
                .collect(Collectors.groupingBy(r -> r.getChambre().getHotel().getNom(), Collectors.counting()))
                .entrySet().stream()
                .map(e -> new ReservationStatsDTO.TopHotelDTO(e.getKey(), e.getValue()))
                .sorted((t1, t2) -> Long.compare(t2.getCount(), t1.getCount()))
                .limit(5)
                .collect(Collectors.toList());

        // Monthly Stats (pour ce partenaire seulement)
        List<ReservationStatsDTO.MonthlyStatsDTO> monthly = all.stream()
                .filter(r -> r.getCreatedAt() != null)
                .collect(Collectors.groupingBy(r -> {
                    LocalDate d = LocalDate.ofInstant(r.getCreatedAt().toInstant(), java.time.ZoneId.systemDefault());
                    return d.getMonth().name();
                }, Collectors.counting()))
                .entrySet().stream()
                .map(e -> new ReservationStatsDTO.MonthlyStatsDTO(e.getKey(), e.getValue()))
                .collect(Collectors.toList());

        return ReservationStatsDTO.builder()
                .total(total)
                .enAttente(enAttente)
                .confirmees(confirmees)
                .refusees(refusees)
                .annulees(annulees)
                .revenusTotal(revenusTotal)
                .topHotels(topHotels)
                .monthly(monthly)
                .build();
    }
}