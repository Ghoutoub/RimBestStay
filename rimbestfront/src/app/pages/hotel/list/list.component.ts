import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { HotelService, Hotel } from '../../../core/services/hotel.service';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-hotel-list',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  templateUrl: './list.component.html'
})
export class HotelListComponent implements OnInit {
  hotels: Hotel[] = [];
  searchTerm = '';
  ville = '';
  etoiles: number | null = null;
  actif: boolean | null = null;
  totalHotels = 0;
  activeHotels = 0;
  totalChambres = 0;
  userRole = '';
  userId: number | null = null;
  statut: 'ACTIF' | 'INACTIF' | null = null;

  constructor(
    private hotelService: HotelService,
    private auth: AuthService
  ) { }

  ngOnInit() {
    this.auth.currentUser$.subscribe(user => {
      if (user) {
        const roleObj = user.roles[0];
        this.userRole = typeof roleObj === 'string' ? roleObj : (roleObj?.name || '');
        this.userId = user.id || null;
      }
      this.loadHotels();
    });
  }

  loadHotels() {

      // Conversion du statut texte en booléen
  if (this.statut === 'ACTIF') {
    this.actif = true;
  } else if (this.statut === 'INACTIF') {
    this.actif = false;
  } else {
    this.actif = null; // Tous les statuts
  }
    const params: any = { size: 1000 };
    if (this.searchTerm) params.search = this.searchTerm;
    if (this.ville) params.ville = this.ville;
    if (this.etoiles) params.etoiles = this.etoiles;
    if (this.actif !== null) params.actif = this.actif;

    this.hotelService.getAll(params).subscribe({
      next: (data: any) => {
        console.log('Hôtels reçus :', data);
        const content = data.content || data;
        this.hotels = content;
        this.totalHotels = data.totalElements || content.length;
        this.activeHotels = content.filter((h: any) => h.actif).length;
        this.totalChambres = content.reduce((acc: number, h: any) => acc + (h.nombreChambres || 0), 0);
      },
      error: (err) => console.error('Erreur chargement hôtels :', err)
    });
  }

  onSearch() {
    this.loadHotels();
  }

  toggleStatus(hotel: Hotel) {
    if (hotel.id) {
      this.hotelService.toggleStatus(hotel.id).subscribe({
        next: (updated) => {
          hotel.actif = updated.actif;
          this.activeHotels = this.hotels.filter(h => h.actif).length;
        },
        error: (err) => console.error('Erreur toggle status :', err)
      });
    }
  }

  confirmDelete(id: number) {
    if (confirm('Supprimer cet hôtel ?')) {
      this.hotelService.delete(id).subscribe(() => this.loadHotels());
    }
  }

  // Removed getImage – use hotel.allImageUrls in template
}