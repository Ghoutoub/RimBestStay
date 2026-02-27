import { Component, inject } from '@angular/core';
import { CommonModule, NgIf, NgFor } from '@angular/common';
import { ReactiveFormsModule, FormBuilder } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { ChambreService, Chambre } from '../../../core/services/chambre.service';
import { HotelService, Hotel } from '../../../core/services/hotel.service';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-chambre-list',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink, NgIf, NgFor],
  templateUrl: './list.component.html',
})
export class ListComponent {
  private fb = inject(FormBuilder);
  private route = inject(ActivatedRoute);
  private chambreService = inject(ChambreService);
  private hotelService = inject(HotelService);
  auth = inject(AuthService);

  hotelId = 0;
  hotel: Hotel | null = null;

  loading = false;
  errorMsg = '';

  chambres: Chambre[] = [];

  // pagination
  currentPage = 0;
  totalPages = 0;
  size = 10;

  form = this.fb.group({
    search: [''],
    type: [''],
    dispo: [''], // '' | 'true' | 'false'
  });

  ngOnInit() {
    this.hotelId = Number(this.route.snapshot.paramMap.get('id'));
    if (!this.hotelId) {
      this.errorMsg = 'ID hôtel invalide.';
      return;
    }
    this.loadHotelHeader();
    this.load(0);
  }

  loadHotelHeader() {
    this.hotelService.getById(this.hotelId).subscribe({
      next: (h) => (this.hotel = h),
      error: () => (this.hotel = null),
    });
  }

  onSearch() {
    this.load(0);
  }

  clearFilters() {
    this.form.reset({ search: '', type: '', dispo: '' });
    this.load(0);
  }

  load(page: number) {
    this.loading = true;
    this.errorMsg = '';

    const v = this.form.value;
    this.chambreService
      .listByHotel(this.hotelId, {
        page,
        size: this.size,
        search: v.search || '',
        type: v.type || '',
        dispo: (v.dispo || '') as any,
      })
      .subscribe({
        next: (res) => {
          this.loading = false;
          this.chambres = res.content || [];
          this.totalPages = res.totalPages ?? 0;
          this.currentPage = res.number ?? 0;
        },
        error: (err) => {
          this.loading = false;
          this.errorMsg = err?.error?.message || 'Erreur lors du chargement des chambres.';
        },
      });
  }

  pagesArray(): number[] {
    return Array.from({ length: this.totalPages }, (_, i) => i);
  }

  confirmDelete(id: number) {
    const ok = confirm('Supprimer cette chambre ?');
    if (!ok) return;

    this.chambreService.delete(id).subscribe({
      next: () => this.load(this.currentPage),
      error: (err) => alert(err?.error?.message || 'Suppression impossible.'),
    });
  }
}
