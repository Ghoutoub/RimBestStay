import { Injectable, inject } from '@angular/core';
import { ApiService } from './api.service';
import { map } from 'rxjs';
import { environment } from '../../../environments/environment';

export type Chambre = {
  id: number;
  numero?: string;
  type?: string;          // "SINGLE", "DOUBLE", ...
  prixParNuit?: number;
  prixNuit?: number;      // ← added for consistency with backend
  capacite?: number;
  disponible?: boolean;
  description?: string;
  hotelId?: number;
  taxeSejour?: number;
  superficie?: number;
  equipementsChambre?: string;
  vueType?: string;
  nombreLits?: number;
  typeLits?: string;
  depotGarantie?: number;
  prixWeekend?: number;
  salleBainPrivee?: boolean;
  climatisation?: boolean;
  television?: boolean;
  wifi?: boolean;
  minibar?: boolean;
  coffreFort?: boolean;
  statutNettoyage?: string;
  imagesChambre?: string;
  allImageUrls?: string[];
};

export type ChambrePage = {
  content: Chambre[];
  totalPages: number;
  number: number;
  totalElements: number;
};

@Injectable({ providedIn: 'root' })
export class ChambreService {
  private api = inject(ApiService);

  // ✅ backend attendu: GET /api/chambres/hotel/{hotelId}?page=&size=&search=
  listByHotel(hotelId: number, params: { page?: number; size?: number; search?: string; type?: string; dispo?: '' | 'true' | 'false' }) {
    const q = new URLSearchParams();
    if (params.page !== undefined) q.set('page', String(params.page));
    if (params.size !== undefined) q.set('size', String(params.size));
    if (params.search) q.set('search', params.search);
    if (params.type) q.set('type', params.type);
    if (params.dispo !== '' && params.dispo !== undefined) q.set('disponible', params.dispo);

    return this.api.get<ChambrePage>(`/chambres/hotel/${hotelId}?${q.toString()}`).pipe(
      map(res => {
        if (res.content) {
          res.content.forEach(c => this.mapChambre(c));
        }
        return res;
      })
    );
  }

  // ✅ backend attendu: DELETE /api/chambres/{id}
  delete(id: number) {
    return this.api.delete(`/chambres/${id}`);
  }


  getById(id: number) {
    return this.api.get<Chambre>(`/chambres/${id}`).pipe(
      map(c => this.mapChambre(c))
    );
  }

  private mapChambre(chambre: any): Chambre {
    if (chambre.imagesChambre) {
      const urls = chambre.imagesChambre.split(',');
      chambre.allImageUrls = urls.map((u: string) => {
        const path = u.trim();
        if (path.startsWith('http')) return path;
        const cleanMediaUrl = environment.mediaUrl.endsWith('/') ? environment.mediaUrl.slice(0, -1) : environment.mediaUrl;
        const cleanPath = path.startsWith('/') ? path : '/' + path;
        return `${cleanMediaUrl}${cleanPath}`;
      });
    } else {
      chambre.allImageUrls = [];
    }
    return chambre;
  }

  create(hotelId: number, payload: Partial<Chambre>) {
    return this.api.post<Chambre>(`/chambres/hotel/${hotelId}`, payload);
  }

  createWithFiles(hotelId: number, formData: FormData) {
    return this.api.post<Chambre>(`/chambres/hotel/${hotelId}/multipart`, formData);
  }

  update(id: number, payload: Partial<Chambre>) {
    return this.api.put<Chambre>(`/chambres/${id}`, payload);
  }

  updateWithFiles(id: number, formData: FormData) {
    return this.api.put<Chambre>(`/chambres/${id}/multipart`, formData);
  }

  availableByHotel(hotelId: number, params: { dateDebut: string; dateFin: string; nbPersonnes?: number }) {
    const q = new URLSearchParams();
    q.set('dateDebut', params.dateDebut);
    q.set('dateFin', params.dateFin);
    if (params.nbPersonnes) q.set('nbPersonnes', String(params.nbPersonnes));

    // ✅ backend attendu : GET /api/chambres/hotel/{id}/disponibles?dateDebut&dateFin&nbPersonnes
    return this.api.get<Chambre[]>(`/chambres/hotel/${hotelId}/disponibles?${q.toString()}`).pipe(
      map(chambres => {
        chambres.forEach(c => this.mapChambre(c));
        return chambres;
      })
    );
  }


}
