import { Injectable } from '@angular/core';
import { ApiService } from './api.service';
import { HttpParams } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface Hotel {
  id?: number;
  nom: string;
  etoiles: number;
  pays: string;
  ville: string;
  adresse?: string;
  telephone: string;
  email: string;
  description?: string;
  actif: boolean;
  imageList?: string[];
  chambres?: any[];
  allImageUrls?: string[];
  partenaire?: any;
  partenaireId?: number;
  equipementsHotel?: string;
  imagesUrls?: string;
  prixParNuit?: number;      // ← ajouté pour la recherche/recommandation
  nombreChambres?: number;
}

@Injectable({ providedIn: 'root' })
export class HotelService {
  constructor(private api: ApiService) { }

  getAll(params?: any): Observable<any> {
    let httpParams = new HttpParams();
    if (params) {
      Object.keys(params).forEach(key => {
        if (params[key] !== null && params[key] !== undefined && params[key] !== '') {
          httpParams = httpParams.set(key, params[key]);
        }
      });
    }
    return this.api.get<any>('/hotels', httpParams).pipe(
      map(res => {
        const content = res.content || res;
        if (Array.isArray(content)) {
          content.forEach(h => this.mapHotel(h));
        } else {
          this.mapHotel(content);
        }
        return res;
      })
    );
  }

  getById(id: number): Observable<Hotel> {
    return this.api.get<Hotel>(`/hotels/${id}`).pipe(
      map(h => this.mapHotel(h))
    );
  }

  private mapHotel(hotel: any): Hotel {
    // Gestion de imagesUrls (peut être une chaîne CSV ou un tableau)
    if (hotel.imagesUrls) {
      let urls: string[];
      if (typeof hotel.imagesUrls === 'string') {
        urls = hotel.imagesUrls.split(',');
      } else if (Array.isArray(hotel.imagesUrls)) {
        urls = hotel.imagesUrls;
      } else {
        urls = [];
      }
      hotel.allImageUrls = urls.map((u: string) => {
        const path = u.trim();
        if (path.startsWith('http')) return path;
        // Strip leading slash from path and trailing slash from mediaUrl if needed
        const cleanMediaUrl = environment.mediaUrl.endsWith('/') ? environment.mediaUrl.slice(0, -1) : environment.mediaUrl;
        const cleanPath = path.startsWith('/') ? path : '/' + path;
        return `${cleanMediaUrl}${cleanPath}`;
      });
    } else {
      hotel.allImageUrls = [];
    }

    // Gestion des images des chambres (inchangée)
    if (hotel.chambres) {
      hotel.chambres.forEach((c: any) => {
        if (c.imagesChambre) {
          c.allImageUrls = c.imagesChambre.split(',').map((u: string) => {
            const path = u.trim();
            if (path.startsWith('http')) return path;
            const cleanMediaUrl = environment.mediaUrl.endsWith('/') ? environment.mediaUrl.slice(0, -1) : environment.mediaUrl;
            const cleanPath = path.startsWith('/') ? path : '/' + path;
            return `${cleanMediaUrl}${cleanPath}`;
          });
        }
      });
    }
    return hotel;
  }
  create(hotel: FormData): Observable<Hotel> {
    return this.api.post<Hotel>('/hotels', hotel);
  }

  update(id: number, hotel: FormData): Observable<Hotel> {
    return this.api.put<Hotel>(`/hotels/${id}`, hotel);
  }

  delete(id: number): Observable<void> {
    return this.api.delete<void>(`/hotels/${id}`);
  }

  toggleStatus(id: number): Observable<Hotel> {
    return this.api.patch<Hotel>(`/hotels/${id}/toggle-status`, {}).pipe(
      map(h => this.mapHotel(h))
    );
  }

  search(params: any): Observable<any> {
    let httpParams = new HttpParams();
    if (params) {
      Object.keys(params).forEach(key => {
        if (params[key] !== null && params[key] !== undefined && params[key] !== '') {
          httpParams = httpParams.set(key, params[key]);
        }
      });
    }
    return this.api.get<any>('/hotels/search', httpParams).pipe(
      map(res => {
        const content = res.content || res;
        if (Array.isArray(content)) {
          content.forEach(h => this.mapHotel(h));
        } else {
          this.mapHotel(content);
        }
        return res;
      })
    );
  }

  getAvailable(params: any): Observable<any> {
    let httpParams = new HttpParams();
    if (params) {
      Object.keys(params).forEach(key => {
        if (params[key] !== null && params[key] !== undefined && params[key] !== '') {
          httpParams = httpParams.set(key, params[key]);
        }
      });
    }
    return this.api.get<any>('/hotels/available', httpParams).pipe(
      map(res => {
        const content = res.content || res;
        if (Array.isArray(content)) {
          content.forEach((h: any) => this.mapHotel(h));
        } else {
          this.mapHotel(content);
        }
        return res;
      })
    );
  }
}