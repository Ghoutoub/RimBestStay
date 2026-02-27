package com.Rimbest.rimbest.model.dto;

import lombok.Data;
import java.util.List;

@Data
public class FilterOptionsDTO {
    private List<String> villes;
    private List<Integer> etoiles;
    private List<String> equipementsHotel;
    private List<String> equipementsChambre;
    private PriceRangeDTO priceRange;
}