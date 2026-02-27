import { Component, inject } from '@angular/core';
import { CommonModule, NgIf, NgFor } from '@angular/common';
import { ReactiveFormsModule, FormBuilder } from '@angular/forms';
import { PartenaireService, PartenaireReservation } from '../../../core/services/partenaire.service';

@Component({
  selector: 'app-partenaire-reservations',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, NgIf, NgFor],
  templateUrl: './reservations.component.html',
})
export class ReservationsComponent {
  private fb = inject(FormBuilder);
  private partenaire = inject(PartenaireService);

  loading = false;
  errorMsg = '';

  reservations: PartenaireReservation[] = [];
  currentPage = 0;
  totalPages = 0;
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
    this.partenaire.myReservations({
      page,
      size: this.size,
      search: v.search || '',
      statut: v.statut || '',
    }).subscribe({
      next: (res) => {
        this.loading = false;
        this.reservations = res.content || [];
        this.totalPages = res.totalPages ?? 0;
        this.currentPage = res.number ?? 0;
      },
      error: (err) => {
        this.loading = false;
        this.errorMsg = err?.error?.message || 'Erreur chargement réservations partenaire.';
      },
    });
  }

  onSearch() {
    this.load(0);
  }

  clear() {
    this.form.reset({ search: '', statut: '' });
    this.load(0);
  }

  pagesArray(): number[] {
    return Array.from({ length: this.totalPages }, (_, i) => i);
  }

  badgeClass(statut: PartenaireReservation['statut']) {
    switch (statut) {
      case 'CONFIRMEE': return 'bg-success';
      case 'REFUSEE': return 'bg-danger';
      case 'ANNULEE': return 'bg-secondary';
      default: return 'bg-warning text-dark';
    }
  }

  setStatus(r: PartenaireReservation, statut: 'CONFIRMEE' | 'REFUSEE') {
    const ok = confirm(`Confirmer l'action: ${statut} pour réservation #${r.id} ?`);
    if (!ok) return;

    this.partenaire.setReservationStatus(r.id, statut).subscribe({
      next: () => this.load(this.currentPage),
      error: (err) => alert(err?.error?.message || 'Action impossible.'),
    });
  }
}
