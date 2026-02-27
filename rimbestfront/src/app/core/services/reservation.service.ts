import { Injectable, inject } from '@angular/core';
import { ApiService } from './api.service';

export type Reservation = {
  id: number;
  dateDebut: string;   // "2026-02-11"
  dateFin: string;
  nbPersonnes?: number;

  statut: 'EN_ATTENTE' | 'CONFIRMEE' | 'REFUSEE' | 'ANNULEE' | 'TERMINEE';

  clientNom?: string;
  clientEmail?: string;
  clientTelephone?: string;

  hotelId?: number;
  hotelNom?: string;

  chambreId?: number;
  chambreNumero?: string;
  chambreType?: string;

  reference?: string;   // ← added
  prixTotal?: number;
  createdAt?: string;
  modePaiement?: string;
  statutPaiement?: string;
};

export type ReservationPage = {
  content: Reservation[];
  totalPages: number;
  number: number;
  totalElements: number;
};

export type ReservationStats = {
  total: number;
  enAttente: number;
  confirmees: number;
  refusees: number;
  annulees: number;

  // optionnel
  revenusTotal?: number;

  // top hôtels
  topHotels?: { hotelNom: string; count: number }[];

  // séries par mois (si backend le fournit)
  monthly?: { month: string; count: number }[];
};

@Injectable({ providedIn: 'root' })
export class ReservationService {
  private api = inject(ApiService);

  // ✅ backend attendu: GET /api/reservations?page=&size=&statut=&search=
  list(params: { page?: number; size?: number; statut?: string; search?: string }) {
    const q = new URLSearchParams();
    if (params.page !== undefined) q.set('page', String(params.page));
    if (params.size !== undefined) q.set('size', String(params.size));
    if (params.statut) q.set('statut', params.statut);
    if (params.search) q.set('search', params.search);
    return this.api.get<ReservationPage>(`/reservations?${q.toString()}`);
  }

  // ✅ GET /api/reservations/client
  listMine() {
    return this.api.get<Reservation[]>('/reservations/client');
  }

  // ✅ GET /api/reservations/{id}
  getById(id: number) {
    return this.api.get<Reservation>(`/reservations/${id}`);
  }

  // ✅ POST /api/reservations
  // payload: { chambreId, dateDebut, dateFin, nbPersonnes }
  create(payload: { chambreId: number; dateDebut: string; dateFin: string; nbPersonnes: number }) {
    return this.api.post<Reservation>(`/reservations`, payload);
  }

  // ✅ PUT /api/reservations/{id}/statut (backend attend activate boolean)
  setStatus(id: number, statut: 'CONFIRMEE' | 'REFUSEE' | 'ANNULEE') {
    const activate = statut === 'CONFIRMEE';
    return this.api.put<Reservation>(`/reservations/${id}/statut`, { activate });
  }
  // ✅ backend attendu: GET /api/reservations/stats
  stats() {
    return this.api.get<ReservationStats>('/reservations/stats');
  }
}




