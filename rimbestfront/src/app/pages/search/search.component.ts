import { Component, inject } from '@angular/core';
import { CommonModule, NgIf, NgFor } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';

import { HotelService, Hotel } from '../../core/services/hotel.service';
import { ChambreService, Chambre } from '../../core/services/chambre.service';
import { ReservationService } from '../../core/services/reservation.service';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-search',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, NgIf, NgFor],
  templateUrl: './search.component.html',
  styleUrls: ['./search.component.css'],
})
export class SearchComponent {
  private fb = inject(FormBuilder);
  private hotelService = inject(HotelService);
  private chambreService = inject(ChambreService);
  private reservationService = inject(ReservationService);
  private router = inject(Router);
  auth = inject(AuthService);

  loadingHotels = false;
  loadingRooms = false;
  creating = false;

  errorMsg = '';
  hotels: Hotel[] = [];
  selectedHotel: Hotel | null = null;

  chambres: Chambre[] = [];
  selectedChambre: Chambre | null = null;

  // Form de recherche
  form = this.fb.group({
    search: [''],
    ville: [''],
    pays: [''],
    etoiles: [''],
    prixMin: [null],
    prixMax: [null],
    wifi: [false],
    climatisation: [false],
    dateDebut: ['', [Validators.required]],
    dateFin: ['', [Validators.required]],
    nbPersonnes: [1, [Validators.required, Validators.min(1), Validators.max(20)]],
  });

  recommendations: any[] = [];

  ngOnInit() {
    this.loadHotels();
  }

  loadRecommendations() {
    this.loadingHotels = true;
    this.errorMsg = '';
    this.recommendations = [];

    const v = this.form.value;
    const params: any = {
      ville: v.ville,
      pays: v.pays,
      etoilesMin: v.etoiles === '' ? null : Number(v.etoiles),
      prixMin: v.prixMin,
      prixMax: v.prixMax,
      capacite: v.nbPersonnes || 1
    };

    if (v.wifi) params.equipement = 'wifi';
    if (v.climatisation) params.equipement = params.equipement ? params.equipement + ',climatisation' : 'climatisation';

    this.hotelService.getAvailable(params).subscribe({
      next: (res: any) => {
        this.loadingHotels = false;
        this.hotels = res?.content ?? res?.page?.content ?? [];
        // Each hotel here already matched the criteria including price and availability
      },
      error: (err) => {
        this.loadingHotels = false;
        this.errorMsg = err?.error?.message || 'Erreur lors de la recherche des recommandations.';
      }
    });
  }

  loadHotels() {
    this.loadRecommendations();
  }

  pickHotel(h: Hotel) {
    this.selectedHotel = h;
    this.chambres = [];
    this.selectedChambre = null;
    this.loadAvailableRooms();
  }

  loadAvailableRooms() {
    this.errorMsg = '';
    if (!this.selectedHotel) return;
    if (this.form.controls.dateDebut.invalid || this.form.controls.dateFin.invalid) {
      this.form.controls.dateDebut.markAsTouched();
      this.form.controls.dateFin.markAsTouched();
      this.errorMsg = 'Choisis des dates valides.';
      return;
    }

    const { dateDebut, dateFin, nbPersonnes } = this.form.value;
    this.loadingRooms = true;

    this.chambreService.availableByHotel(this.selectedHotel.id!, {
      dateDebut: dateDebut!,
      dateFin: dateFin!,
      nbPersonnes: nbPersonnes ?? 1,
    }).subscribe({
      next: (rooms) => {
        this.loadingRooms = false;
        this.chambres = rooms || [];
        this.selectedChambre = null;
      },
      error: (err) => {
        this.loadingRooms = false;
        this.errorMsg = err?.error?.message || 'Erreur chargement chambres disponibles.';
      }
    });
  }

  pickRoom(c: Chambre) {
    this.selectedChambre = c;
  }

  viewRoomDetails(c: Chambre) {
    const v = this.form.value;
    this.router.navigate(['/chambre/details', c.id], {
      queryParams: {
        dateDebut: v.dateDebut,
        dateFin: v.dateFin,
        nbPersonnes: v.nbPersonnes
      }
    });
  }
}
