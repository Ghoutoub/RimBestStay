import { Component, inject } from '@angular/core';
import { CommonModule, NgIf, NgFor } from '@angular/common';
import { RouterLink } from '@angular/router';
import { InvoiceService, Invoice } from '../../../core/services/invoice.service';

@Component({
  selector: 'app-invoices-list',
  standalone: true,
  imports: [CommonModule, RouterLink, NgIf, NgFor],
  templateUrl: './list.component.html',
})
export class ListComponent {
  private invoices = inject(InvoiceService);

  loading = false;
  errorMsg = '';

  items: Invoice[] = [];
  currentPage = 0;
  totalPages = 0;
  size = 10;

  ngOnInit() {
    this.load(0);
  }

  load(page: number) {
    this.loading = true;
    this.errorMsg = '';
    this.invoices.listMine({ page, size: this.size }).subscribe({
      next: (res) => {
        this.loading = false;
        this.items = res.content || [];
        this.totalPages = res.totalPages ?? 0;
        this.currentPage = res.number ?? 0;
      },
      error: (err) => {
        this.loading = false;
        this.errorMsg = err?.error?.message || 'Erreur chargement factures.';
      }
    });
  }

  pagesArray(): number[] {
    return Array.from({ length: this.totalPages }, (_, i) => i);
  }

  badgeClass(statut?: string) {
    switch (statut) {
      case 'PAID': return 'bg-success';
      case 'CANCELLED': return 'bg-secondary';
      default: return 'bg-warning text-dark';
    }
  }
}
