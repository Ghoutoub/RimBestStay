package com.Rimbest.rimbest.model;

import jakarta.persistence.*;

@Entity
@Table(name = "administrators")
@PrimaryKeyJoinColumn(name = "user_id")
// @DiscriminatorValue("ADMIN")
public  class Administrator extends User {
    
    @Column(length = 50)
    private String departement;
    
    // Constructeur
    public Administrator() {}
    
    public Administrator(String nom, String email, String motDePasse, String departement) {
        super(nom, email, motDePasse);
        this.departement = departement;
    }
    
    // Getter et Setter
    public String getDepartement() { return departement; }
    public void setDepartement(String departement) { this.departement = departement; }
}