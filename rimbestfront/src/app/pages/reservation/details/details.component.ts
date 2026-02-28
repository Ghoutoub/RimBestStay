import { Component, inject } from '@angular/core';
import { CommonModule, NgIf } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { ReservationService, Reservation } from '../../../core/services/reservation.service';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-reservation-details',
  standalone: true,
  imports: [CommonModule, RouterLink, NgIf],
  templateUrl: './details.component.html',
})
export class DetailsComponent {
  private route = inject(ActivatedRoute);
  private reservationService = inject(ReservationService);
  auth = inject(AuthService);

  id = 0;
  loading = false;
  errorMsg = '';
  reservation: Reservation | null = null;

  ngOnInit() {
    this.id = Number(this.route.snapshot.paramMap.get('id'));
    if (!this.id) {
      this.errorMsg = 'ID réservation invalide.';
      return;
    }
    this.load();
  }

  load() {
    this.loading = true;
    this.errorMsg = '';
    this.reservationService.getById(this.id).subscribe({
      next: (r) => {
        this.loading = false;
        this.reservation = r;
      },
      error: (err) => {
        this.loading = false;
        this.errorMsg = err?.error?.message || 'Erreur chargement réservation.';
      },
    });
  }

  canManage(): boolean {
    return this.auth.hasAnyRole(['ADMIN', 'PARTENAIRE']);
  }

  setStatus(statut: 'CONFIRMEE' | 'REFUSEE' | 'ANNULEE') {
    if (!this.reservation) return;
    const ok = confirm(`Confirmer l'action: ${statut} ?`);
    if (!ok) return;

    this.loading = true;
    this.reservationService.setStatus(this.reservation.id, statut).subscribe({
      next: (r) => {
        this.loading = false;
        this.reservation = r;
      },
      error: (err) => {
        this.loading = false;
        alert(err?.error?.message || 'Action impossible.');
      },
    });
  }

  getPaiementStatutTexte(statutPaiement?: string, statutReservation?: string): string {
    if (statutPaiement === 'PAYE') return 'Payé intégralement';
    if (statutPaiement === 'PARTIELLEMENT_PAYE') return 'Acompte versé';
    if (statutPaiement === 'ANNULE') return 'Paiement annulé';

    // Fallback sur le statut de réservation si pas de statut paiement
    switch (statutReservation) {
      case 'CONFIRMEE': return 'Payé / Confirmé';
      case 'TERMINEE': return 'Finalisé';
      case 'ANNULEE': return 'Non payé';
      case 'REFUSEE': return 'Refusé / Remboursé';
      default: return 'En attente de paiement';
    }
  }

  getModePaiementTexte(mode?: string): string {
    if (!mode) return 'Paiement sur place';
    switch (mode.toUpperCase()) {
      case 'ESPECES':
      case 'CASH': return 'Espèces / Cash';
      case 'CARTE_BANCAIRE':
      case 'CARD': return 'Carte Bancaire (En ligne)';
      case 'VIREMENT': return 'Virement Bancaire';
      default: return mode;
    }
  }

  print() {
    window.print();
  }
}
