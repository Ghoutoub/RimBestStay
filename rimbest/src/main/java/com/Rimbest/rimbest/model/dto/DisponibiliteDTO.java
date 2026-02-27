package com.Rimbest.rimbest.model.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class DisponibiliteDTO {
    private Long chambreId;
    private String chambreNumero;
    private String typeChambre;
    private Integer capacite;
    private BigDecimal prixNuit;
    private List<LocalDate> datesReservees;
    private boolean disponible;
    
    public boolean isDisponiblePourPeriode(LocalDate dateDebut, LocalDate dateFin) {
        if (!disponible) return false;
        
        for (LocalDate date = dateDebut; date.isBefore(dateFin); date = date.plusDays(1)) {
            if (datesReservees.contains(date)) {
                return false;
            }
        }
        return true;
    }
}