import { Component, inject } from '@angular/core';
import { CommonModule, NgIf, NgFor } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, Validators, AbstractControl, ValidationErrors } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';

import { ReservationService } from '../../../core/services/reservation.service';
import { HotelService, Hotel } from '../../../core/services/hotel.service';
import { ChambreService, Chambre } from '../../../core/services/chambre.service';

function dateRangeValidator(group: AbstractControl): ValidationErrors | null {
  const d1 = group.get('dateDebut')?.value;
  const d2 = group.get('dateFin')?.value;
  if (!d1 || !d2) return null;

  const start = new Date(d1);
  const end = new Date(d2);

  if (isNaN(start.getTime()) || isNaN(end.getTime())) return { dateInvalid: true };
  if (end <= start) return { dateRange: true };

  return null;
}

@Component({
  selector: 'app-reservation-add',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, NgIf, NgFor],
  templateUrl: './add.component.html',
})
export class AddComponent {
  private fb = inject(FormBuilder);
  private reservationService = inject(ReservationService);
  private hotelService = inject(HotelService);
  private chambreService = inject(ChambreService);
  private router = inject(Router);
  private route = inject(ActivatedRoute);

  errorMsg = '';
  okMsg = '';

  loadingHotels = false;
  loadingRooms = false;
  creating = false;

  hotels: Hotel[] = [];
  chambres: Chambre[] = [];
  selectedChambre: Chambre | null = null;

  form = this.fb.group({
    // filtres hôtels
    hotelSearch: [''],
    hotelVille: [''],
    hotelEtoiles: [''],

    // sélection hôtel
    hotelId: [null as any, [Validators.required]],

    // données réservation
    dateDebut: ['', [Validators.required]],
    dateFin: ['', [Validators.required]],
    nbPersonnes: [1, [Validators.required, Validators.min(1), Validators.max(20)]],

    // sélection chambre
    chambreId: [null as any, [Validators.required]],
  }, { validators: [dateRangeValidator] });

  ngOnInit() {
    this.loadHotels();

    const routeHotelId = this.route.snapshot.queryParamMap.get('hotelId');
    const routeChambreId = this.route.snapshot.queryParamMap.get('chambreId');
    const routeDateDebut = this.route.snapshot.queryParamMap.get('dateDebut');
    const routeDateFin = this.route.snapshot.queryParamMap.get('dateFin');
    const routeNbPersonnes = this.route.snapshot.queryParamMap.get('nbPersonnes');

    if (routeDateDebut) this.form.patchValue({ dateDebut: routeDateDebut });
    if (routeDateFin) this.form.patchValue({ dateFin: routeDateFin });
    if (routeNbPersonnes) this.form.patchValue({ nbPersonnes: Number(routeNbPersonnes) });

    if (routeHotelId) {
      this.form.patchValue({ hotelId: Number(routeHotelId) });
      if (routeChambreId) {
        this.loadAvailableRooms();
        this.form.patchValue({ chambreId: Number(routeChambreId) });
      }
    }

    // quand chambreId change → on garde l’objet sélectionné (utile pour le total)
    this.form.controls.chambreId.valueChanges.subscribe((id) => {
      const n = Number(id);
      this.selectedChambre = this.chambres.find(c => c.id === n) || null;
    });
  }

  // ====== helpers calcul (comme l'ancien template) ======
  nights(): number {
    const d1 = this.form.value.dateDebut;
    const d2 = this.form.value.dateFin;
    if (!d1 || !d2) return 0;

    const start = new Date(d1);
    const end = new Date(d2);
    if (isNaN(start.getTime()) || isNaN(end.getTime())) return 0;

    const ms = end.getTime() - start.getTime();
    const nights = Math.floor(ms / (1000 * 60 * 60 * 24));
    return nights > 0 ? nights : 0;
  }

  taxeSejour(): number {
    // comme l'ancien: défaut 20 si non fourni
    const t = this.selectedChambre?.taxeSejour;
    return typeof t === 'number' ? t : 20;
  }

  total(): number {
    const nights = this.nights();
    const prix = this.selectedChambre?.prixParNuit ?? 0;
    if (!this.selectedChambre || nights === 0) return 0;

    // ancien template : total = nuits*prixNuit + taxeSejour (selon impl)
    // si ta taxe est par séjour (une seule fois) :
    return (nights * prix) + this.taxeSejour();
  }

  // ====== hôtels ======
  loadHotels() {
    this.loadingHotels = true;
    this.errorMsg = '';
    this.okMsg = '';

    const v = this.form.value;
    const etoilesVal = v.hotelEtoiles === '' ? '' : Number(v.hotelEtoiles);

    this.hotelService.getAll({
      page: 0,
      size: 50,
      search: v.hotelSearch || '',
      ville: v.hotelVille || '',
      etoiles: etoilesVal as any,
    }).subscribe({
      next: (res: any) => {
        this.loadingHotels = false;
        this.hotels = res?.content ?? res?.page?.content ?? res ?? [];

        // reset choix hôtel/chambre si on vient pas d'un paramètre
        if (!this.form.value.hotelId) {
          this.form.patchValue({ hotelId: null, chambreId: null });
          this.chambres = [];
          this.selectedChambre = null;
        }
      },
      error: (err) => {
        this.loadingHotels = false;
        this.errorMsg = err?.error?.message || 'Erreur chargement hôtels.';
      }
    });
  }

  onHotelChange() {
    this.form.patchValue({ chambreId: null });
    this.chambres = [];
    this.selectedChambre = null;
  }

  // ====== chambres disponibles ======
  loadAvailableRooms() {
    this.errorMsg = '';
    this.okMsg = '';

    if (this.form.controls.hotelId.invalid) {
      this.form.controls.hotelId.markAsTouched();
      this.errorMsg = 'Choisis un hôtel.';
      return;
    }

    // validation dateRange (dateFin > dateDebut)
    if (this.form.hasError('dateRange')) {
      this.errorMsg = 'Date fin doit être après date début.';
      this.form.controls.dateDebut.markAsTouched();
      this.form.controls.dateFin.markAsTouched();
      return;
    }

    if (this.form.controls.dateDebut.invalid || this.form.controls.dateFin.invalid) {
      this.errorMsg = 'Choisis des dates valides.';
      return;
    }

    const hotelId = Number(this.form.value.hotelId);
    const dateDebut = this.form.value.dateDebut!;
    const dateFin = this.form.value.dateFin!;
    const nbPersonnes = this.form.value.nbPersonnes ?? 1;

    this.loadingRooms = true;
    this.chambreService.availableByHotel(hotelId, { dateDebut, dateFin, nbPersonnes }).subscribe({
      next: (rooms) => {
        this.loadingRooms = false;
        this.chambres = rooms || [];
        this.form.patchValue({ chambreId: null });
        this.selectedChambre = null;

        if (this.chambres.length === 0) {
          this.okMsg = 'Aucune chambre disponible pour ces dates.';
        }
      },
      error: (err) => {
        this.loadingRooms = false;
        this.errorMsg = err?.error?.message || 'Erreur chargement chambres disponibles.';
      }
    });
  }

  // ====== créer réservation ======
  submit() {
    this.errorMsg = '';
    this.okMsg = '';

    if (this.form.invalid) {
      this.form.markAllAsTouched();
      if (this.form.hasError('dateRange')) this.errorMsg = 'Date fin doit être après date début.';
      return;
    }

    const payload = {
      chambreId: Number(this.form.value.chambreId),
      dateDebut: this.form.value.dateDebut!,
      dateFin: this.form.value.dateFin!,
      nbPersonnes: this.form.value.nbPersonnes ?? 1,
    };

    this.creating = true;
    this.reservationService.create(payload).subscribe({
      next: (created) => {
        this.creating = false;
        this.router.navigate(['/reservations', created.id]);
      },
      error: (err) => {
        this.creating = false;
        this.errorMsg = err?.error?.message || 'Création réservation impossible.';
      }
    });
  }
}
