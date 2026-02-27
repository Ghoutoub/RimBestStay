package com.Rimbest.rimbest.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReservationStatsDTO {
    private long total;
    private long enAttente;
    private long confirmees;
    private long refusees;
    private long annulees;
    private BigDecimal revenusTotal;
    private List<TopHotelDTO> topHotels;
    private List<MonthlyStatsDTO> monthly;

    @Data
    @AllArgsConstructor
    public static class TopHotelDTO {
        private String hotelNom;
        private long count;
    }

    @Data
    @AllArgsConstructor
    public static class MonthlyStatsDTO {
        private String month;
        private long count;
    }
}
