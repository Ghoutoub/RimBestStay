package com.Rimbest.rimbest.model;

import jakarta.persistence.*;

@Entity
@Table(name = "clients")
@PrimaryKeyJoinColumn(name = "user_id")
// @DiscriminatorValue("CLIENT")
public class Client extends User {

    // Constructeurs
    public Client() {
    }

    public Client(String nom, String email, String motDePasse, String telephone, String adresse) {
        super(nom, email, motDePasse);
        this.setTelephone(telephone);
        this.setAdresse(adresse);
    }
}