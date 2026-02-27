package com.Rimbest.rimbest.model.dto;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PriceRangeDTO {
    private Double min;
    private Double max;
}