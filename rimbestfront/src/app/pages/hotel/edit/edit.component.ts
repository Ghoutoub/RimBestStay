import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { HotelService } from '../../../core/services/hotel.service';

@Component({
  selector: 'app-hotel-edit',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule],
  templateUrl: './edit.component.html'
})
export class HotelEditComponent implements OnInit {
  hotelForm: FormGroup;
  hotelId!: number;
  imageFiles: File[] = [];
  existingImages: { url: string, path: string }[] = [];

  constructor(
    private fb: FormBuilder,
    private route: ActivatedRoute,
    private router: Router,
    private hotelService: HotelService
  ) {
    this.hotelForm = this.fb.group({
      nom: [''],
      etoiles: [3],
      pays: [''],
      ville: [''],
      adresse: [''],
      telephone: [''],
      email: [''],
      description: [''],
      equipementsHotel: [''],
      actif: [true]
    });
  }

  ngOnInit() {
    this.hotelId = +this.route.snapshot.paramMap.get('id')!;
    this.hotelService.getById(this.hotelId).subscribe(hotel => {
      this.hotelForm.patchValue(hotel);
      if (hotel.allImageUrls && hotel.imagesUrls) {
        const rawPaths = hotel.imagesUrls.split(',');
        this.existingImages = hotel.allImageUrls.map((url, i) => ({
          url: url,
          path: rawPaths[i]
        }));
      }
    });
  }

  removeExistingImage(index: number) {
    this.existingImages.splice(index, 1);
  }

  onFileSelected(event: any) {
    this.imageFiles = Array.from(event.target.files);
  }

  onSubmit() {

    const formData = new FormData();
    const hotelData = { ...this.hotelForm.value };

    // Ajouter les URLs des images conservées
    hotelData.imagesUrls = this.existingImages.map(img => img.path);

    formData.append('hotel', new Blob([JSON.stringify(hotelData)], { type: 'application/json' }));

    this.imageFiles.forEach(file => formData.append('imageFiles', file));

    this.hotelService.update(this.hotelId, formData).subscribe({
      next: () => this.router.navigate(['/hotel/list']),
      error: err => console.error(err)
    });
  }
}