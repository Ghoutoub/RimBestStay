package com.Rimbest.rimbest.model.dto;

import lombok.Data;
import java.util.List;

@Data
public class SearchStatsDTO {
    private Long totalHotels;
    private Long hotelsActifs;
    private Long totalChambres;
    private List<TopDestinationDTO> topDestinations;
}