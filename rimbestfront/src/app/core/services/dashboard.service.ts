import { Injectable, inject } from '@angular/core';
import { ApiService } from './api.service';

export type DashboardStats = {
  totalHotels?: number;
  activeHotels?: number;
  inactiveHotels?: number;
  totalChambres?: number;
  totalReservations?: number;
  reservationsEnAttente?: number;
  reservationsConfirmees?: number;
  reservationsRefusees?: number;
  revenusTotal?: number;

  // Admin specific
  totalUsers?: number;
  clientsCount?: number;
  partenairesCount?: number;
  reservationsToday?: number;
  revenueMonth?: number;
  revenueYear?: number;
  revenueTarget?: number;
  revenueHistory?: { month: string; revenue: number }[];

  // Client specific
  pendingReservations?: number;

  // Partenaire specific
  myHotelsCount?: number;
  availableChambres?: number;
  reservationGrowth?: number;
  totalRevenue?: number;
  monthlyRevenue?: number;
};

export type DashboardReservationItem = {
  id: number;
  hotelNom?: string;
  chambreNumero?: string;
  dateDebut: string;
  dateFin: string;
  statut: string;
  clientNom?: string;
  prixTotal?: number;
};

export type DashboardHotelItem = {
  id: number;
  nom: string;
  ville: string;
  etoiles: number;
  minPrice: number;
};

export type DashboardResponse = {
  stats: DashboardStats;
  lastReservations: DashboardReservationItem[];
  recommendedHotels?: DashboardHotelItem[];
};

@Injectable({ providedIn: 'root' })
export class DashboardService {
  private api = inject(ApiService);

  admin() {
    return this.api.get<DashboardResponse>('/dashboard/admin');
  }

  partenaire() {
    return this.api.get<DashboardResponse>('/dashboard/partenaire');
  }

  client() {
    return this.api.get<DashboardResponse>('/dashboard/client');
  }
}
