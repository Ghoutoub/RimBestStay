package com.Rimbest.rimbest.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChambreSearchDTO {
    private Long hotelId;
    private LocalDate dateArrivee;
    private LocalDate dateDepart;
    private Integer adultes = 1;
    private Integer enfants = 0;
    private Integer chambres = 1;
    private Double prixMin;
    private Double prixMax;
    private String typeChambre;
    private String[] equipements;
}