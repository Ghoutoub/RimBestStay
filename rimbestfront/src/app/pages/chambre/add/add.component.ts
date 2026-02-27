import { Component, inject } from '@angular/core';
import { CommonModule, NgIf, NgFor } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { ChambreService } from '../../../core/services/chambre.service';
import { HotelService, Hotel } from '../../../core/services/hotel.service';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-chambre-add',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink, NgIf, NgFor],
  templateUrl: './add.component.html',
})
export class AddComponent {
  private fb = inject(FormBuilder);
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private chambreService = inject(ChambreService);
  private hotelService = inject(HotelService);
  auth = inject(AuthService);

  hotelId = 0;
  hotel: Hotel | null = null;

  loading = false;
  errorMsg = '';
  okMsg = '';

  // Images
  imageFiles: File[] = [];
  imagePreviews: string[] = [];

  form = this.fb.group({
    // Informations de base
    numero: ['', [Validators.required]],
    type: ['DOUBLE', [Validators.required]],
    capacite: [2, [Validators.required, Validators.min(1), Validators.max(20)]],
    prixParNuit: [0, [Validators.required, Validators.min(0)]],
    prixWeekend: [null as number | null],
    superficie: [null as number | null],
    nombreLits: [1, [Validators.required, Validators.min(1)]],
    typeLits: [''],
    vueType: [''],

    // Disponibilité & statut
    disponible: [true],
    salleBainPrivee: [true],
    climatisation: [true],
    television: [true],
    wifi: [true],
    minibar: [false],
    coffreFort: [false],
    statutNettoyage: ['PROPRE'],

    // Tarification
    taxeSejour: [null as number | null],
    depotGarantie: [null as number | null],

    // Textes
    description: [''],
    equipementsChambre: [''],
  });

  ngOnInit() {
    this.hotelId = Number(this.route.snapshot.paramMap.get('id'));
    if (!this.hotelId) {
      this.errorMsg = 'ID hôtel invalide.';
      return;
    }
    this.hotelService.getById(this.hotelId).subscribe({
      next: (h) => (this.hotel = h),
      error: () => (this.hotel = null),
    });
  }

  onFileSelected(event: any) {
    const files: File[] = Array.from(event.target.files);
    this.imageFiles = files;
    this.imagePreviews = [];
    files.forEach((f) => {
      const reader = new FileReader();
      reader.onload = (e) => this.imagePreviews.push(e.target?.result as string);
      reader.readAsDataURL(f);
    });
  }

  submit() {
    this.errorMsg = '';
    this.okMsg = '';

    if (this.form.invalid) {
      this.form.markAllAsTouched();
      window.scrollTo(0, 0);
      return;
    }

    this.loading = true;
    const payload = this.form.value;

    if (this.imageFiles.length > 0) {
      const fd = new FormData();
      fd.append('chambre', new Blob([JSON.stringify(payload)], { type: 'application/json' }));
      this.imageFiles.forEach((f) => fd.append('imageFiles', f));
      this.chambreService.createWithFiles(this.hotelId, fd).subscribe({
        next: () => this.onSuccess(),
        error: (err) => this.onError(err),
      });
    } else {
      this.chambreService.create(this.hotelId, payload as any).subscribe({
        next: () => this.onSuccess(),
        error: (err) => this.onError(err),
      });
    }
  }

  private onSuccess() {
    this.loading = false;
    this.okMsg = '✅ Chambre ajoutée avec succès.';
    setTimeout(() => this.router.navigate(['/chambre/list', this.hotelId]), 1200);
  }

  private onError(err: any) {
    this.loading = false;
    this.errorMsg = err?.error?.message || "Erreur lors de l'ajout de la chambre.";
  }
}
