import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { HotelService } from '../../../core/services/hotel.service';

@Component({
  selector: 'app-hotel-add',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule],
  templateUrl: './add.component.html'
})
export class HotelAddComponent {
  form: FormGroup;
  imageFiles: File[] = [];

  constructor(
    private fb: FormBuilder,
    private hotelService: HotelService,
    private router: Router
  ) {
    this.form = this.fb.group({
      nom: ['', Validators.required],
      etoiles: [3],
      pays: ['', Validators.required],
      ville: ['', Validators.required],
      adresse: [''],
      telephone: ['', Validators.required],
      email: ['', [Validators.required, Validators.email]],
      description: [''],
      actif: [true]
    });
  }

  onFileSelected(event: any) {
    this.imageFiles = Array.from(event.target.files);
  }

  onSubmit() {
    if (this.form.invalid) return;

    const formData = new FormData();

    // Créer l'objet JSON à partir du formulaire
    const hotelData = { ...this.form.value };
    // Supprimer les éventuels champs vides si nécessaire, mais garder la structure
    formData.append('hotel', new Blob([JSON.stringify(hotelData)], { type: 'application/json' }));

    // Ajouter les fichiers
    this.imageFiles.forEach(file => formData.append('imageFiles', file));

    this.hotelService.create(formData).subscribe({
      next: () => this.router.navigate(['/hotel/list']),
      error: err => console.error(err)
    });
  }
}