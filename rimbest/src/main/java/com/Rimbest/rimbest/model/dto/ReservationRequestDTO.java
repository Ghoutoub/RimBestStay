package com.Rimbest.rimbest.model.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public class ReservationRequestDTO {

    @NotNull(message = "L'ID de la chambre est obligatoire")
    private Long chambreId;

    @NotNull(message = "La date de début est obligatoire")
    @FutureOrPresent(message = "La date de début doit être aujourd'hui ou dans le futur")
    private LocalDate dateDebut;

    @NotNull(message = "La date de fin est obligatoire")
    @Future(message = "La date de fin doit être dans le futur")
    private LocalDate dateFin;

    @Min(value = 1, message = "Le nombre de personnes doit être au moins 1")
    private Integer nbPersonnes = 1;

    private String notesSpeciales;

    // Getters and Setters
    public Long getChambreId() {
        return chambreId;
    }

    public void setChambreId(Long chambreId) {
        this.chambreId = chambreId;
    }

    public LocalDate getDateDebut() {
        return dateDebut;
    }

    public void setDateDebut(LocalDate dateDebut) {
        this.dateDebut = dateDebut;
    }

    public LocalDate getDateFin() {
        return dateFin;
    }

    public void setDateFin(LocalDate dateFin) {
        this.dateFin = dateFin;
    }

    public Integer getNbPersonnes() {
        return nbPersonnes;
    }

    public void setNbPersonnes(Integer nbPersonnes) {
        this.nbPersonnes = nbPersonnes;
    }

    public String getNotesSpeciales() {
        return notesSpeciales;
    }

    public void setNotesSpeciales(String notesSpeciales) {
        this.notesSpeciales = notesSpeciales;
    }
}