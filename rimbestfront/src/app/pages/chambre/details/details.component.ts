import { Component, inject } from '@angular/core';
import { CommonModule, NgIf } from '@angular/common';
import { ActivatedRoute, RouterLink, Router } from '@angular/router';
import { ChambreService, Chambre } from '../../../core/services/chambre.service';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-chambre-details',
  standalone: true,
  imports: [CommonModule, RouterLink, NgIf],
  templateUrl: './details.component.html',
})
export class DetailsComponent {
  private route = inject(ActivatedRoute);
  private chambreService = inject(ChambreService);
  auth = inject(AuthService);
  private router = inject(Router);

  id = 0;
  loading = false;
  errorMsg = '';
  chambre: Chambre | null = null;

  dateDebut = '';
  dateFin = '';
  nbPersonnes = 1;

  ngOnInit() {
    this.id = Number(this.route.snapshot.paramMap.get('id'));
    this.dateDebut = this.route.snapshot.queryParamMap.get('dateDebut') || '';
    this.dateFin = this.route.snapshot.queryParamMap.get('dateFin') || '';
    this.nbPersonnes = Number(this.route.snapshot.queryParamMap.get('nbPersonnes')) || 1;

    if (!this.id) {
      this.errorMsg = 'ID chambre invalide.';
      return;
    }
    this.load();
  }

  toggling = false;

  load() {
    this.loading = true;
    this.chambreService.getById(this.id).subscribe({
      next: (c) => {
        this.loading = false;
        this.chambre = c;
      },
      error: (err) => {
        this.loading = false;
        this.errorMsg = err?.error?.message || 'Erreur chargement chambre.';
      },
    });
  }

  toggleAvailability() {
    if (!this.chambre) return;
    this.toggling = true;
    const newStatus = !this.chambre.disponible;
    this.chambreService.update(this.id, { disponible: newStatus }).subscribe({
      next: (updated) => {
        this.toggling = false;
        if (this.chambre) {
          this.chambre.disponible = updated.disponible;
        }
      },
      error: (err) => {
        this.toggling = false;
        alert(err?.error?.message || 'Erreur lors de la modification de la disponibilité.');
      }
    });
  }

  bookRoom() {
    if (!this.auth.isLoggedIn()) {
      this.router.navigate(['/login'], { queryParams: { returnUrl: this.route.snapshot.url.join('/') } });
      return;
    }
    this.router.navigate(['/reservations/add'], {
      queryParams: {
        hotelId: this.chambre?.hotelId,
        chambreId: this.chambre?.id,
        dateDebut: this.dateDebut,
        dateFin: this.dateFin,
        nbPersonnes: this.nbPersonnes
      }
    });
  }
}
