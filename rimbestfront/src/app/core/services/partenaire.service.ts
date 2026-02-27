import { Injectable, inject } from '@angular/core';
import { ApiService } from './api.service';
import { Hotel } from './hotel.service';
import { Chambre } from './chambre.service';

export type HotelPage = {
  content: Hotel[];
  totalPages: number;
  number: number;
  totalElements: number;
};

export type PartenaireChambre = Chambre & {
  hotelId?: number;
  hotelNom?: string;
};

export type PartenaireChambrePage = {
  content: PartenaireChambre[];
  totalPages: number;
  number: number;
  totalElements: number;
};

export type MyHotelMini = { id: number; nom: string };

export type PartenaireReservation = {
  id: number;
  hotelNom?: string;
  chambreNumero?: string;
  chambreType?: string;
  dateDebut: string;
  dateFin: string;
  nbPersonnes?: number;
  statut: 'EN_ATTENTE' | 'CONFIRMEE' | 'REFUSEE' | 'ANNULEE';
  clientNom?: string;
  clientEmail?: string;
  prixTotal?: number;
};

export type PartenaireReservationPage = {
  content: PartenaireReservation[];
  totalPages: number;
  number: number;
  totalElements: number;
};

@Injectable({ providedIn: 'root' })
export class PartenaireService {
  private api = inject(ApiService);

  // ✅ backend attendu: GET /api/partenaire/hotels?page=&size=&search=
  myHotels(params: { page?: number; size?: number; search?: string }) {
    const q = new URLSearchParams();
    if (params.page !== undefined) q.set('page', String(params.page));
    if (params.size !== undefined) q.set('size', String(params.size));
    if (params.search) q.set('search', params.search);

    return this.api.get<HotelPage>(`/partenaire/hotels?${q.toString()}`);
  }
    // ✅ GET /api/partenaire/reservations?page=&size=&statut=&search=
  myReservations(params: { page?: number; size?: number; statut?: string; search?: string }) {
    const q = new URLSearchParams();
    if (params.page !== undefined) q.set('page', String(params.page));
    if (params.size !== undefined) q.set('size', String(params.size));
    if (params.statut) q.set('statut', params.statut);
    if (params.search) q.set('search', params.search);

    return this.api.get<PartenaireReservationPage>(`/partenaire/reservations?${q.toString()}`);
  }

  // ✅ PUT /api/partenaire/reservations/{id}/status
  setReservationStatus(id: number, statut: 'CONFIRMEE' | 'REFUSEE') {
    return this.api.put(`/partenaire/reservations/${id}/status`, { statut });
  }
  // ✅ GET /api/partenaire/hotels/min
  myHotelsMini() {
    return this.api.get<MyHotelMini[]>(`/partenaire/hotels/min`);
  }

  // ✅ GET /api/partenaire/chambres?page=&size=&hotelId=&search=
  myChambres(params: { page?: number; size?: number; hotelId?: number | ''; search?: string }) {
    const q = new URLSearchParams();
    if (params.page !== undefined) q.set('page', String(params.page));
    if (params.size !== undefined) q.set('size', String(params.size));
    if (params.search) q.set('search', params.search);
    if (params.hotelId !== '' && params.hotelId !== undefined && params.hotelId !== null) q.set('hotelId', String(params.hotelId));

    return this.api.get<PartenaireChambrePage>(`/partenaire/chambres?${q.toString()}`);
  }
}






