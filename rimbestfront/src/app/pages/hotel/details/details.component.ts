import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterModule, Router } from '@angular/router';
import { HotelService, Hotel } from '../../../core/services/hotel.service';
import { ChambreService, Chambre } from '../../../core/services/chambre.service';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-hotel-details',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './details.component.html'
})
export class HotelDetailsComponent implements OnInit {
  hotel: Hotel | null = null;
  userRole = '';

  constructor(
    private route: ActivatedRoute,
    private hotelService: HotelService,
    private chambreService: ChambreService,
    private auth: AuthService,
    private router: Router
  ) { }

  ngOnInit() {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.hotelService.getById(+id).subscribe(data => {
        this.hotel = data;
        // Fetch rooms since they are no longer included in the HotelResponse automatically
        this.chambreService.listByHotel(+id, { size: 100 }).subscribe(chambrePage => {
          if (this.hotel) {
            this.hotel.chambres = chambrePage.content;
          }
        });
      });
    }
    this.auth.currentUser$.subscribe(u => {
      this.userRole = u?.roles[0]?.name || '';
    });
  }

  bookRoom(ch: any) {
    if (!this.auth.isLoggedIn()) {
      this.router.navigate(['/login'], { queryParams: { returnUrl: this.route.snapshot.url.join('/') } });
      return;
    }
    this.router.navigate(['/reservations/add'], { queryParams: { hotelId: this.hotel?.id, chambreId: ch.id } });
  }

  viewRoomDetails(ch: any) {
    this.router.navigate(['/chambre/details', ch.id]);
  }

  confirmDelete(id: number) {
    if (confirm('Supprimer cet hôtel ?')) {
      this.hotelService.delete(id).subscribe(() => window.history.back());
    }
  }

  // Removed redundant image helpers – use this.hotel.allImageUrls in template

  deleteChambre(chambreId: number) {
    // À implémenter si vous avez un service pour supprimer une chambre
    if (confirm('Supprimer cette chambre ?')) {
      // this.chambreService.delete(chambreId).subscribe(...);
    }
  }

  goToRooms() {
    const tabEl = document.getElementById('rooms-tab');
    if (tabEl) {
      tabEl.click();
      tabEl.scrollIntoView({ behavior: 'smooth', block: 'start' });
    }
  }
}