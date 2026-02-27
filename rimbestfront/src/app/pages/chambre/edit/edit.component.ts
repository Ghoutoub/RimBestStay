import { Component, inject } from '@angular/core';
import { CommonModule, NgIf, NgFor } from '@angular/common';
import { ReactiveFormsModule, FormBuilder } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { ChambreService, Chambre } from '../../../core/services/chambre.service';

@Component({
  selector: 'app-chambre-edit',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink, NgIf, NgFor],
  templateUrl: './edit.component.html',
})
export class EditComponent {
  private fb = inject(FormBuilder);
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private chambreService = inject(ChambreService);

  id = 0;
  chambre: Chambre | null = null;

  loading = false;
  errorMsg = '';
  okMsg = '';

  // Images
  imageFiles: File[] = [];
  existingImages: { url: string; path: string }[] = [];
  imagePreviews: string[] = [];

  form = this.fb.group({
    // Informations de base
    numero: [''],
    type: ['DOUBLE'],
    capacite: [1 as number | null],
    prixParNuit: [0 as number | null],
    prixWeekend: [null as number | null],
    superficie: [null as number | null],
    nombreLits: [1 as number | null],
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
    this.id = Number(this.route.snapshot.paramMap.get('id'));
    if (!this.id) { this.errorMsg = 'ID chambre invalide.'; return; }
    this.load();
  }

  load() {
    this.loading = true;
    this.chambreService.getById(this.id).subscribe({
      next: (c) => {
        this.loading = false;
        this.chambre = c;
        this.form.patchValue({
          numero: c.numero ?? '',
          type: c.type ?? 'DOUBLE',
          capacite: c.capacite ?? 1,
          prixParNuit: c.prixNuit ?? c.prixParNuit ?? 0,
          prixWeekend: c.prixWeekend ?? null,
          superficie: c.superficie ?? null,
          nombreLits: c.nombreLits ?? 1,
          typeLits: c.typeLits ?? '',
          vueType: c.vueType ?? '',
          disponible: c.disponible !== false,
          salleBainPrivee: c.salleBainPrivee !== false,
          climatisation: c.climatisation !== false,
          television: c.television !== false,
          wifi: c.wifi !== false,
          minibar: c.minibar === true,
          coffreFort: c.coffreFort === true,
          statutNettoyage: c.statutNettoyage ?? 'PROPRE',
          taxeSejour: c.taxeSejour ?? null,
          depotGarantie: c.depotGarantie ?? null,
          description: c.description ?? '',
          equipementsChambre: c.equipementsChambre ?? '',
        });

        // Charger images existantes
        if (c.allImageUrls && c.imagesChambre) {
          const paths = c.imagesChambre.split(',');
          this.existingImages = c.allImageUrls.map((url, i) => ({
            url,
            path: paths[i] ?? '',
          }));
        }
      },
      error: (err) => {
        this.loading = false;
        this.errorMsg = err?.error?.message || 'Erreur chargement chambre.';
      },
    });
  }

  removeExistingImage(index: number) {
    this.existingImages.splice(index, 1);
  }

  onFileSelected(event: any) {
    const files: File[] = Array.from(event.target.files);
    this.imageFiles = files;
    this.imagePreviews = [];
    files.forEach(f => {
      const reader = new FileReader();
      reader.onload = (e) => this.imagePreviews.push(e.target?.result as string);
      reader.readAsDataURL(f);
    });
  }

  submit() {
    this.errorMsg = '';
    this.okMsg = '';
    this.loading = true;

    const payload: any = { ...this.form.value };

    // Ajouter les chemins des images conservées
    payload.imagesUrls = this.existingImages.map(img => img.path);

    // Si des nouvelles images → FormData, sinon JSON simple
    if (this.imageFiles.length > 0) {
      const fd = new FormData();
      fd.append('chambre', new Blob([JSON.stringify(payload)], { type: 'application/json' }));
      this.imageFiles.forEach(f => fd.append('imageFiles', f));
      this.chambreService.updateWithFiles(this.id, fd).subscribe({
        next: () => this.onSuccess(),
        error: (err) => this.onError(err),
      });
    } else {
      this.chambreService.update(this.id, payload as any).subscribe({
        next: () => this.onSuccess(),
        error: (err) => this.onError(err),
      });
    }
  }

  private onSuccess() {
    this.loading = false;
    this.okMsg = '✅ Chambre modifiée avec succès.';
    setTimeout(() => this.router.navigate(['/chambre/details', this.id]), 1200);
  }

  private onError(err: any) {
    this.loading = false;
    this.errorMsg = err?.error?.message || 'Erreur lors de la modification.';
  }
}
