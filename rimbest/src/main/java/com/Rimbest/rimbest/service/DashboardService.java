package com.Rimbest.rimbest.service;

import com.Rimbest.rimbest.model.*;
import com.Rimbest.rimbest.model.dto.DashboardResponse;
import com.Rimbest.rimbest.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    @Autowired
    private HotelRepository hotelRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private RoleRepository roleRepository;

    public DashboardResponse getAdminStats() {
        DashboardResponse response = new DashboardResponse();
        DashboardResponse.DashboardStats stats = new DashboardResponse.DashboardStats();

        long totalHotels = hotelRepository.count();
        long activeHotels = hotelRepository.countByActifTrue();
        stats.setTotalHotels(totalHotels);
        stats.setActiveHotels(activeHotels);
        stats.setInactiveHotels(totalHotels - activeHotels);
        stats.setTotalUsers(userRepository.count());
        stats.setTotalReservations(reservationRepository.count());
        stats.setReservationsEnAttente(reservationRepository.countByStatut(StatutReservation.EN_ATTENTE));

        LocalDate today = LocalDate.now();
        stats.setReservationsToday(reservationRepository.findByDateRange(today, today).stream().count());

        Role clientRole = roleRepository.findByName(ERole.ROLE_CLIENT).orElse(null);
        Role partenaireRole = roleRepository.findByName(ERole.ROLE_PARTENAIRE).orElse(null);

        if (clientRole != null)
            stats.setClientsCount(userRepository.countByRolesContaining(clientRole));
        if (partenaireRole != null)
            stats.setPartenairesCount(userRepository.countByRolesContaining(partenaireRole));

        // Revenue
        BigDecimal totalRevenue = reservationRepository.getTotalRevenue();
        stats.setTotalRevenue(totalRevenue != null ? totalRevenue.doubleValue() : 0.0);

        BigDecimal revenueMonth = reservationRepository.getRevenueByPartenaireAndMonth(null, today.getYear(),
                today.getMonthValue());
        stats.setRevenueMonth(revenueMonth != null ? revenueMonth.doubleValue() : 0.0);

        stats.setRevenueTarget(200000.0); // Realistic target

        // Revenue history for last 6 months
        java.util.List<DashboardResponse.DashboardStats.MonthlyRevenueDTO> history = new java.util.ArrayList<>();
        for (int i = 5; i >= 0; i--) {
            LocalDate d = today.minusMonths(i);
            BigDecimal rev = reservationRepository.getRevenueByPartenaireAndMonth(null, d.getYear(), d.getMonthValue());
            history.add(new DashboardResponse.DashboardStats.MonthlyRevenueDTO(d.getMonth().name().substring(0, 3),
                    rev != null ? rev.doubleValue() : 0.0));
        }
        stats.setRevenueHistory(history);

        response.setStats(stats);
        response.setLastReservations(mapReservations(
                reservationRepository.findAllByOrderByDateArriveeDesc(PageRequest.of(0, 8)).getContent()));

        return response;
    }

    public DashboardResponse getPartenaireStats(User partenaire) {
        DashboardResponse response = new DashboardResponse();
        DashboardResponse.DashboardStats stats = new DashboardResponse.DashboardStats();

        long myHotels = hotelRepository.countByPartenaire(partenaire);
        stats.setMyHotelsCount(myHotels);
        stats.setTotalHotels(myHotels);

        // Count my rooms via hotels
        long totalChambers = hotelRepository.findByPartenaire(partenaire).stream()
                .mapToLong(h -> h.getChambres() != null ? h.getChambres().size() : 0).sum();
        long availableChambers = hotelRepository.findByPartenaire(partenaire).stream()
                .flatMap(h -> h.getChambres() != null ? h.getChambres().stream() : java.util.stream.Stream.empty())
                .filter(c -> c.getDisponible()).count();

        stats.setTotalChambres(totalChambers);
        stats.setAvailableChambres(availableChambers);

        List<Reservation> myReservations = reservationRepository.findByPartenaire(partenaire);
        stats.setTotalReservations((long) myReservations.size());
        stats.setReservationsEnAttente(
                myReservations.stream().filter(r -> r.getStatut() == StatutReservation.EN_ATTENTE).count());

        BigDecimal totalRevenue = reservationRepository.getTotalRevenueByPartenaire(partenaire);
        stats.setTotalRevenue(totalRevenue != null ? totalRevenue.doubleValue() : 0.0);

        LocalDate today = LocalDate.now();
        BigDecimal revMonth = reservationRepository.getRevenueByPartenaireAndMonth(partenaire, today.getYear(),
                today.getMonthValue());
        stats.setRevenueMonth(revMonth != null ? revMonth.doubleValue() : 0.0);

        // Simple growth calc (compared to previous month)
        LocalDate lastMonth = today.minusMonths(1);
        BigDecimal revLastMonth = reservationRepository.getRevenueByPartenaireAndMonth(partenaire, lastMonth.getYear(),
                lastMonth.getMonthValue());
        double growth = 0;
        if (revLastMonth != null && revLastMonth.doubleValue() > 0) {
            growth = ((stats.getRevenueMonth() - revLastMonth.doubleValue()) / revLastMonth.doubleValue()) * 100;
        } else if (stats.getRevenueMonth() > 0) {
            growth = 100.0;
        }
        stats.setReservationGrowth(growth);

        response.setStats(stats);
        response.setLastReservations(mapReservations(
                myReservations.stream().sorted((r1, r2) -> r2.getId().compareTo(r1.getId())).limit(8)
                        .collect(Collectors.toList())));

        return response;
    }

    public DashboardResponse getClientStats(User client) {
        DashboardResponse response = new DashboardResponse();
        DashboardResponse.DashboardStats stats = new DashboardResponse.DashboardStats();

        List<Reservation> clientReservations = reservationRepository.findByClient(client);
        stats.setTotalReservations((long) clientReservations.size());
        stats.setPendingReservations(
                clientReservations.stream().filter(r -> r.getStatut() == StatutReservation.EN_ATTENTE).count());
        stats.setReservationsEnAttente(stats.getPendingReservations());

        response.setStats(stats);
        response.setLastReservations(
                mapReservations(clientReservations.stream().sorted((r1, r2) -> r2.getId().compareTo(r1.getId()))
                        .limit(5).collect(Collectors.toList())));

        // Add recommended hotels
        List<Hotel> recommendations = hotelRepository.findRecommendedHotels(PageRequest.of(0, 4));
        response.setRecommendedHotels(mapHotels(recommendations));

        return response;
    }

    private List<DashboardResponse.DashboardReservationItem> mapReservations(List<Reservation> reservations) {
        return reservations.stream().map(r -> {
            DashboardResponse.DashboardReservationItem item = new DashboardResponse.DashboardReservationItem();
            item.setId(r.getId());
            item.setHotelNom(r.getChambre().getHotel().getNom());
            item.setClientNom(r.getClient().getNom());
            item.setDateDebut(r.getDateArrivee().toString());
            item.setDateFin(r.getDateDepart().toString());
            item.setStatut(r.getStatut().toString());
            item.setPrixTotal(r.getPrixTotal().doubleValue());
            return item;
        }).collect(Collectors.toList());
    }

    private List<DashboardResponse.DashboardHotelItem> mapHotels(List<Hotel> hotels) {
        return hotels.stream().map(h -> {
            DashboardResponse.DashboardHotelItem item = new DashboardResponse.DashboardHotelItem();
            item.setId(h.getId());
            item.setNom(h.getNom());
            item.setVille(h.getVille());
            item.setEtoiles(h.getEtoiles());
            item.setMinPrice(h.getChambres() != null && !h.getChambres().isEmpty()
                    ? h.getChambres().stream().mapToDouble(c -> c.getPrixNuit()).min().orElse(0)
                    : 0);
            return item;
        }).collect(Collectors.toList());
    }
}
