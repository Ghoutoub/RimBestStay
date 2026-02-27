import { Injectable, inject } from '@angular/core';
import { ApiService } from './api.service';

export type Invoice = {
  id: number;
  reservationId?: number;
  date?: string;
  statut?: 'DUE' | 'PAID' | 'CANCELLED';
  montant?: number;
  devise?: string; // MRU ou EUR etc
};

export type InvoicePage = {
  content: Invoice[];
  totalPages: number;
  number: number;
  totalElements: number;
};

@Injectable({ providedIn: 'root' })
export class InvoiceService {
  private api = inject(ApiService);

  // ✅ GET /api/invoices?page=&size=
  listMine(params: { page?: number; size?: number }) {
    const q = new URLSearchParams();
    if (params.page !== undefined) q.set('page', String(params.page));
    if (params.size !== undefined) q.set('size', String(params.size));
    return this.api.get<InvoicePage>(`/invoices?${q.toString()}`);
  }

  // ✅ GET /api/invoices/{id}
  getById(id: number) {
    return this.api.get<Invoice>(`/invoices/${id}`);
  }

  // ✅ POST /api/invoices/{id}/pay  (paiement “simulé” ou réel selon backend)
  pay(id: number, payload?: { method?: 'CASH' | 'CARD' | 'MOBILE' }) {
    return this.api.post(`/invoices/${id}/pay`, payload ?? { method: 'CASH' });
  }
}
