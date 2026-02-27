package com.Rimbest.rimbest.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ChangePasswordDTO {
    
    @NotBlank(message = "Le mot de passe actuel est obligatoire")
    private String currentPassword;
    
    @NotBlank(message = "Le nouveau mot de passe est obligatoire")
    @Size(min = 6, message = "Le mot de passe doit contenir au moins 6 caractères")
    private String newPassword;
    
    @NotBlank(message = "La confirmation du mot de passe est obligatoire")
    private String confirmPassword;
    
    // Getters et Setters
    public String getCurrentPassword() { return currentPassword; }
    public void setCurrentPassword(String currentPassword) { this.currentPassword = currentPassword; }
    
    public String getNewPassword() { return newPassword; }
    public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
    
    public String getConfirmPassword() { return confirmPassword; }
    public void setConfirmPassword(String confirmPassword) { this.confirmPassword = confirmPassword; }
}