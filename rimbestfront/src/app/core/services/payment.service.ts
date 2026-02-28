import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface PaymentDetails {
    cardNumber?: string;
    expiryDate?: string;
    cvv?: string;
    cardHolderName?: string;
    method: 'CARD' | 'CASH';
}

@Injectable({
    providedIn: 'root'
})
export class PaymentService {
    private apiUrl = `${environment.apiBaseUrl}/paiements`;

    constructor(private http: HttpClient) { }

    processPayment(reservationId: number, details: PaymentDetails): Observable<any> {
        return this.http.post(`${this.apiUrl}/process/${reservationId}`, details);
    }
}
