package com.Rimbest.rimbest.model.dto;

import com.Rimbest.rimbest.model.Hotel;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class AdminHotelStatsDTO {
    private Hotel hotel;
    private Long reservationCount;
    private BigDecimal revenue;
    private Double occupancyRate;
}