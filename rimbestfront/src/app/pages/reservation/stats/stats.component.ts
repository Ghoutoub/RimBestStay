import { Component, inject } from '@angular/core';
import { CommonModule, NgIf, NgFor } from '@angular/common';
import { RouterLink } from '@angular/router';
import { ReservationService, ReservationStats } from '../../../core/services/reservation.service';

@Component({
  selector: 'app-reservation-stats',
  standalone: true,
  imports: [CommonModule, RouterLink, NgIf, NgFor],
  templateUrl: './stats.component.html',
})
export class StatsComponent {
  private reservationService = inject(ReservationService);

  loading = false;
  errorMsg = '';
  data: ReservationStats | null = null;

  ngOnInit() {
    this.load();
  }

  load() {
    this.loading = true;
    this.errorMsg = '';
    this.reservationService.stats().subscribe({
      next: (res: ReservationStats | null) => {
        this.loading = false;
        this.data = res;
      },
      error: (err: { error: { message: string; }; }) => {
        this.loading = false;
        this.errorMsg = err?.error?.message || 'Erreur chargement statistiques.';
      },
    });
  }

  pct(n: number) {
    const total = this.data?.total ?? 0;
    if (!total) return 0;
    return Math.round((n / total) * 100);
  }
}
