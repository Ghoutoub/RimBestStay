import { Component, inject } from '@angular/core';
import { CommonModule, NgIf, NgFor } from '@angular/common';
import { ReactiveFormsModule, FormBuilder } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { PartenaireService, PartenaireChambre, MyHotelMini } from '../../../core/services/partenaire.service';

@Component({
  selector: 'app-partenaire-chambres',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink, NgIf, NgFor],
  templateUrl: './chambres.component.html',
})
export class ChambresComponent {
  private fb = inject(FormBuilder);
  private partenaire = inject(PartenaireService);

  loading = false;
  errorMsg = '';

  hotels: MyHotelMini[] = [];
  chambres: PartenaireChambre[] = [];

  currentPage = 0;
  totalPages = 0;
  size = 10;

  form = this.fb.group({
    hotelId: [''],
    search: [''],
  });

  ngOnInit() {
    this.loadHotelsMini();
    this.load(0);
  }

  loadHotelsMini() {
    this.partenaire.myHotelsMini().subscribe({
      next: (list) => (this.hotels = list || []),
      error: () => (this.hotels = []),
    });
  }

  load(page: number) {
    this.loading = true;
    this.errorMsg = '';

    const v = this.form.value;
    const hotelId = v.hotelId === '' ? '' : Number(v.hotelId);

    this.partenaire.myChambres({
      page,
      size: this.size,
      search: v.search || '',
      hotelId: hotelId as any,
    }).subscribe({
      next: (res) => {
        this.loading = false;
        this.chambres = res.content || [];
        this.totalPages = res.totalPages ?? 0;
        this.currentPage = res.number ?? 0;
      },
      error: (err) => {
        this.loading = false;
        this.errorMsg = err?.error?.message || 'Erreur chargement chambres partenaire.';
      },
    });
  }

  onSearch() {
    this.load(0);
  }

  clear() {
    this.form.reset({ hotelId: '', search: '' });
    this.load(0);
  }
}
