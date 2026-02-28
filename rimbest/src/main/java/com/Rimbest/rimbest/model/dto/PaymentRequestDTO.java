package com.Rimbest.rimbest.model.dto;

import lombok.Data;

@Data
public class PaymentRequestDTO {
    private String cardNumber;
    private String expiryDate;
    private String cvv;
    private String cardHolderName;
    private String method; // e.g., "CARD", "CASH"
}
