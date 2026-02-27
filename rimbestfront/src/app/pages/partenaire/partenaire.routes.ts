import { Routes } from '@angular/router';
import { authGuard } from '../../core/guards/auth.guard';
import { roleGuard } from '../../core/guards/role.guard';

export const PARTENAIRE_ROUTES: Routes = [
  {
    path: '',
    canActivate: [authGuard, roleGuard(['ROLE_PARTENAIRE'])],
    children: [
      // Vous ajouterez ici les routes pour les composants partenaire
      // Exemple :
      // { path: 'hotels', component: PartenaireHotelsComponent },
      // { path: 'reservations', component: PartenaireReservationsComponent }
    ]
  }
];