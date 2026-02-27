import { Component, inject } from '@angular/core';
import { CommonModule, NgIf, NgFor } from '@angular/common';
import { ReactiveFormsModule, FormBuilder } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { PartenaireService } from '../../../core/services/partenaire.service';
import { Hotel } from '../../../core/services/hotel.service';

@Component({
  selector: 'app-partenaire-hotels',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink, NgIf, NgFor],
  templateUrl: './hotels.component.html',
})
export class HotelsComponent {
  private fb = inject(FormBuilder);
  private partenaire = inject(PartenaireService);

  loading = false;
  errorMsg = '';

  hotels: Hotel[] = [];
  currentPage = 0;
  totalPages = 0;
  size = 10;

  form = this.fb.group({
    search: [''],
  });

  ngOnInit() {
    this.load(0);
  }

  load(page: number) {
    this.loading = true;
    this.errorMsg = '';

    const v = this.form.value;
    this.partenaire.myHotels({ page, size: this.size, search: v.search || '' }).subscribe({
      next: (res) => {
        this.loading = false;
        this.hotels = res.content || [];
        this.totalPages = res.totalPages ?? 0;
        this.currentPage = res.number ?? 0;
      },
      error: (err) => {
        this.loading = false;
        this.errorMsg = err?.error?.message || 'Erreur chargement de vos hôtels.';
      },
    });
  }

  onSearch() {
    this.load(0);
  }

  pagesArray(): number[] {
    return Array.from({ length: this.totalPages }, (_, i) => i);
  }
}
