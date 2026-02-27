package com.Rimbest.rimbest.model.dto;

import java.util.List;

public class DashboardResponse {
    private DashboardStats stats;
    private List<DashboardReservationItem> lastReservations;
    private List<DashboardHotelItem> recommendedHotels;

    public DashboardResponse() {
    }

    public DashboardStats getStats() {
        return stats;
    }

    public void setStats(DashboardStats stats) {
        this.stats = stats;
    }

    public List<DashboardReservationItem> getLastReservations() {
        return lastReservations;
    }

    public void setLastReservations(List<DashboardReservationItem> lastReservations) {
        this.lastReservations = lastReservations;
    }

    public List<DashboardHotelItem> getRecommendedHotels() {
        return recommendedHotels;
    }

    public void setRecommendedHotels(List<DashboardHotelItem> recommendedHotels) {
        this.recommendedHotels = recommendedHotels;
    }

    public static class DashboardStats {
        private Long totalHotels;
        private Long activeHotels;
        private Long inactiveHotels;
        private Long totalChambres;
        private Long availableChambres;
        private Long totalReservations;
        private Long reservationsEnAttente;
        private Long reservationsToday;
        private Long totalUsers;
        private Long clientsCount;
        private Long partenairesCount;
        private Double revenueMonth;
        private Double revenueYear;
        private Double totalRevenue;
        private Double revenueTarget;
        private Double reservationGrowth;
        private List<MonthlyRevenueDTO> revenueHistory;

        private Long myHotelsCount;
        private Long pendingReservations;

        // Getters and Setters
        public Long getTotalHotels() {
            return totalHotels;
        }

        public void setTotalHotels(Long totalHotels) {
            this.totalHotels = totalHotels;
        }

        public Long getActiveHotels() {
            return activeHotels;
        }

        public void setActiveHotels(Long activeHotels) {
            this.activeHotels = activeHotels;
        }

        public Long getInactiveHotels() {
            return inactiveHotels;
        }

        public void setInactiveHotels(Long inactiveHotels) {
            this.inactiveHotels = inactiveHotels;
        }

        public Long getTotalChambres() {
            return totalChambres;
        }

        public void setTotalChambres(Long totalChambres) {
            this.totalChambres = totalChambres;
        }

        public Long getAvailableChambres() {
            return availableChambres;
        }

        public void setAvailableChambres(Long availableChambres) {
            this.availableChambres = availableChambres;
        }

        public Long getTotalReservations() {
            return totalReservations;
        }

        public void setTotalReservations(Long totalReservations) {
            this.totalReservations = totalReservations;
        }

        public Long getReservationsEnAttente() {
            return reservationsEnAttente;
        }

        public void setReservationsEnAttente(Long reservationsEnAttente) {
            this.reservationsEnAttente = reservationsEnAttente;
        }

        public Long getReservationsToday() {
            return reservationsToday;
        }

        public void setReservationsToday(Long reservationsToday) {
            this.reservationsToday = reservationsToday;
        }

        public Long getTotalUsers() {
            return totalUsers;
        }

        public void setTotalUsers(Long totalUsers) {
            this.totalUsers = totalUsers;
        }

        public Long getClientsCount() {
            return clientsCount;
        }

        public void setClientsCount(Long clientsCount) {
            this.clientsCount = clientsCount;
        }

        public Long getPartenairesCount() {
            return partenairesCount;
        }

        public void setPartenairesCount(Long partenairesCount) {
            this.partenairesCount = partenairesCount;
        }

        public Double getRevenueMonth() {
            return revenueMonth;
        }

        public void setRevenueMonth(Double revenueMonth) {
            this.revenueMonth = revenueMonth;
        }

        public Double getRevenueYear() {
            return revenueYear;
        }

        public void setRevenueYear(Double revenueYear) {
            this.revenueYear = revenueYear;
        }

        public Double getTotalRevenue() {
            return totalRevenue;
        }

        public void setTotalRevenue(Double totalRevenue) {
            this.totalRevenue = totalRevenue;
        }

        public Double getRevenueTarget() {
            return revenueTarget;
        }

        public void setRevenueTarget(Double revenueTarget) {
            this.revenueTarget = revenueTarget;
        }

        public Double getReservationGrowth() {
            return reservationGrowth;
        }

        public void setReservationGrowth(Double reservationGrowth) {
            this.reservationGrowth = reservationGrowth;
        }

        public List<MonthlyRevenueDTO> getRevenueHistory() {
            return revenueHistory;
        }

        public void setRevenueHistory(List<MonthlyRevenueDTO> revenueHistory) {
            this.revenueHistory = revenueHistory;
        }

        public Long getMyHotelsCount() {
            return myHotelsCount;
        }

        public void setMyHotelsCount(Long myHotelsCount) {
            this.myHotelsCount = myHotelsCount;
        }

        public Long getPendingReservations() {
            return pendingReservations;
        }

        public void setPendingReservations(Long pendingReservations) {
            this.pendingReservations = pendingReservations;
        }

        public static class MonthlyRevenueDTO {
            private String month;
            private Double revenue;

            public MonthlyRevenueDTO(String month, Double revenue) {
                this.month = month;
                this.revenue = revenue;
            }

            public String getMonth() {
                return month;
            }

            public void setMonth(String month) {
                this.month = month;
            }

            public Double getRevenue() {
                return revenue;
            }

            public void setRevenue(Double revenue) {
                this.revenue = revenue;
            }
        }
    }

    public static class DashboardReservationItem {
        private Long id;
        private String hotelNom;
        private String clientNom;
        private String dateDebut;
        private String dateFin;
        private String statut;
        private Double prixTotal;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getHotelNom() {
            return hotelNom;
        }

        public void setHotelNom(String hotelNom) {
            this.hotelNom = hotelNom;
        }

        public String getClientNom() {
            return clientNom;
        }

        public void setClientNom(String clientNom) {
            this.clientNom = clientNom;
        }

        public String getDateDebut() {
            return dateDebut;
        }

        public void setDateDebut(String dateDebut) {
            this.dateDebut = dateDebut;
        }

        public String getDateFin() {
            return dateFin;
        }

        public void setDateFin(String dateFin) {
            this.dateFin = dateFin;
        }

        public String getStatut() {
            return statut;
        }

        public void setStatut(String statut) {
            this.statut = statut;
        }

        public Double getPrixTotal() {
            return prixTotal;
        }

        public void setPrixTotal(Double prixTotal) {
            this.prixTotal = prixTotal;
        }
    }

    public static class DashboardHotelItem {
        private Long id;
        private String nom;
        private String ville;
        private Integer etoiles;
        private Double minPrice;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getNom() {
            return nom;
        }

        public void setNom(String nom) {
            this.nom = nom;
        }

        public String getVille() {
            return ville;
        }

        public void setVille(String ville) {
            this.ville = ville;
        }

        public Integer getEtoiles() {
            return etoiles;
        }

        public void setEtoiles(Integer etoiles) {
            this.etoiles = etoiles;
        }

        public Double getMinPrice() {
            return minPrice;
        }

        public void setMinPrice(Double minPrice) {
            this.minPrice = minPrice;
        }
    }
}
