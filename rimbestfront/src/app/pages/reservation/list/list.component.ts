import { Component, inject } from '@angular/core';
import { CommonModule, NgIf, NgFor } from '@angular/common';
import { ReactiveFormsModule, FormBuilder } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { ReservationService, Reservation } from '../../../core/services/reservation.service';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-reservation-list',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink, NgIf, NgFor],
  templateUrl: './list.component.html',
})
export class ListComponent {
  private fb = inject(FormBuilder);
  private reservationService = inject(ReservationService);
  auth = inject(AuthService);

  loading = false;
  errorMsg = '';

  reservations: Reservation[] = [];

  // pagination
  currentPage = 0;
  totalPages = 0;
  totalElements = 0;
  size = 10;

  form = this.fb.group({
    search: [''],
    statut: [''],
  });

  ngOnInit() {
    this.load(0);
  }

  load(page: number) {
    this.loading = true;
    this.errorMsg = '';

    const v = this.form.value;

    if (this.auth.hasRole('CLIENT')) {
      // Les clients utilisent un endpoint différent qui renvoie un tableau non encapsulé dans "content"
      this.reservationService.listMine().subscribe({
        next: (res: Reservation[]) => {
          this.loading = false;
          // Si on veut faire du filtrage côté client puisqu'on a tout le tableau:
          let filtered = res || [];
          if (v.statut) filtered = filtered.filter(r => r.statut === v.statut);
          this.reservations = filtered;
          this.totalElements = filtered.length;
          this.totalPages = 1;
          this.currentPage = 0;
        },
        error: (err: any) => {
          this.loading = false;
          this.errorMsg = err?.error?.message || 'Erreur chargement réservations (Client).';
        }
      });
    } else {
      // Admin et Partenaires utilisent l'endpoint paginé
      this.reservationService
        .list({
          page,
          size: this.size,
          search: v.search || '',
          statut: v.statut || '',
        })
        .subscribe({
          next: (res: any) => {
            this.loading = false;
            this.reservations = res.content || [];
            this.totalPages = res.totalPages ?? 0;
            this.totalElements = res.totalElements ?? res.content?.length ?? 0;
            this.currentPage = res.number ?? 0;
          },
          error: (err: any) => {
            this.loading = false;
            this.errorMsg = err?.error?.message || 'Erreur chargement réservations.';
          },
        });
    }
  }

  onSearch() {
    this.load(0);
  }

  clearFilters() {
    this.form.reset({ search: '', statut: '' });
    this.load(0);
  }

  setStatut(statut: string) {
    this.form.get('statut')?.setValue(statut);
    this.load(0);
  }

  pagesArray(): number[] {
    return Array.from({ length: this.totalPages }, (_, i) => i);
  }

  badgeClass(statut: Reservation['statut']) {
    switch (statut) {
      case 'CONFIRMEE': return 'bg-success';
      case 'REFUSEE': return 'bg-danger';
      case 'ANNULEE': return 'bg-secondary';
      default: return 'bg-warning text-dark';
    }
  }
}
