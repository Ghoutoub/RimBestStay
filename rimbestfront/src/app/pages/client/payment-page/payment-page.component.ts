import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { PaymentService } from '../../../core/services/payment.service';
import { ReservationService } from '../../../core/services/reservation.service';

@Component({
    selector: 'app-payment-page',
    standalone: true,
    imports: [CommonModule, ReactiveFormsModule, RouterModule],
    templateUrl: './payment-page.component.html',
    styleUrl: './payment-page.component.css'
})
export class PaymentPageComponent implements OnInit {
    reservationId: number = 0;
    reservation: any = null;
    paymentForm: FormGroup;
    paymentMethod: 'CARD' | 'CASH' = 'CARD';
    loading = false;
    success = false;
    error: string | null = null;

    constructor(
        private route: ActivatedRoute,
        private router: Router,
        private fb: FormBuilder,
        private paymentService: PaymentService,
        private reservationService: ReservationService
    ) {
        this.paymentForm = this.fb.group({
            cardNumber: ['', [Validators.required, Validators.minLength(12)]],
            cardHolderName: ['', [Validators.required]],
            expiryDate: ['', [Validators.required]],
            cvv: ['', [Validators.required, Validators.minLength(3)]]
        });
    }

    ngOnInit() {
        this.reservationId = Number(this.route.snapshot.paramMap.get('id'));
        if (!this.reservationId) {
            this.router.navigate(['/reservations/client']);
            return;
        }

        this.reservationService.getById(this.reservationId).subscribe({
            next: (res: any) => this.reservation = res,
            error: () => this.router.navigate(['/reservations/client'])
        });
    }

    setMethod(method: 'CARD' | 'CASH') {
        this.paymentMethod = method;
        if (method === 'CASH') {
            this.paymentForm.disable();
        } else {
            this.paymentForm.enable();
        }
    }

    onSubmit() {
        if (this.paymentMethod === 'CARD' && this.paymentForm.invalid) return;

        this.loading = true;
        this.error = null;

        const details = {
            ...this.paymentForm.value,
            method: this.paymentMethod
        };

        this.paymentService.processPayment(this.reservationId, details).subscribe({
            next: (resp) => {
                this.loading = false;
                this.success = true;
                setTimeout(() => {
                    this.router.navigate(['/reservation/details', this.reservationId]);
                }, 10000);
            },
            error: (err) => {
                this.loading = false;
                this.error = err.error?.message || "Une erreur est survenue lors du paiement.";
            }
        });
    }
}
