import { Routes } from '@angular/router';
import { authGuard } from '../../core/guards/auth.guard';
import { roleGuard } from '../../core/guards/role.guard';

export const CLIENT_ROUTES: Routes = [
  {
    path: '',
    canActivate: [authGuard, roleGuard(['ROLE_CLIENT'])],
    children: [
      // { path: 'reservations', component: ClientReservationsComponent },
      // { path: 'profile', component: ClientProfileComponent }
    ]
  }
];