import { Component, inject } from '@angular/core';
import { CommonModule, NgIf } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { InvoiceService, Invoice } from '../../../core/services/invoice.service';

@Component({
  selector: 'app-invoice-details',
  standalone: true,
  imports: [CommonModule, RouterLink, NgIf],
  templateUrl: './details.component.html',
})
export class DetailsComponent {
  private route = inject(ActivatedRoute);
  private invoices = inject(InvoiceService);

  loading = false;
  paying = false;
  errorMsg = '';

  invoice: Invoice | null = null;

  ngOnInit() {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.load(id);
  }

  load(id: number) {
    this.loading = true;
    this.errorMsg = '';
    this.invoices.getById(id).subscribe({
      next: (res) => {
        this.loading = false;
        this.invoice = res;
      },
      error: (err) => {
        this.loading = false;
        this.errorMsg = err?.error?.message || 'Facture introuvable.';
      }
    });
  }

  pay() {
    if (!this.invoice) return;
    if (this.invoice.statut === 'PAID') return;

    const ok = confirm(`Payer la facture #${this.invoice.id} ?`);
    if (!ok) return;

    this.paying = true;
    this.invoices.pay(this.invoice.id, { method: 'CASH' }).subscribe({
      next: () => {
        this.paying = false;
        this.load(this.invoice!.id);
      },
      error: (err) => {
        this.paying = false;
        this.errorMsg = err?.error?.message || 'Paiement impossible.';
      }
    });
  }

  badgeClass(statut?: string) {
    switch (statut) {
      case 'PAID': return 'bg-success';
      case 'CANCELLED': return 'bg-secondary';
      default: return 'bg-warning text-dark';
    }
  }
}
