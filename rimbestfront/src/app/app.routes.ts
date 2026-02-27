import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';
import { roleGuard } from './core/guards/role.guard';
import { MainLayoutComponent } from './layout/main-layout/main-layout.component';
import { DashboardComponent } from './pages/dashboard/dashboard.component';

export const routes: Routes = [
  {
    path: '',
    component: MainLayoutComponent,
    children: [
      { path: '', loadComponent: () => import('./pages/home/home.component').then(m => m.HomeComponent) },
      { path: 'login', loadComponent: () => import('./pages/auth/login/login.component').then(m => m.LoginComponent) },
      { path: 'inscription', loadComponent: () => import('./pages/auth/inscription/inscription.component').then(m => m.InscriptionComponent) },
      {
        path: 'hotel',
        children: [
          { path: 'list', loadComponent: () => import('./pages/hotel/list/list.component').then(m => m.HotelListComponent) },
          { path: 'details/:id', loadComponent: () => import('./pages/hotel/details/details.component').then(m => m.HotelDetailsComponent) },
          { path: 'add', loadComponent: () => import('./pages/hotel/add/add.component').then(m => m.HotelAddComponent), canActivate: [roleGuard(['ROLE_ADMIN', 'ROLE_PARTENAIRE'])] },
          { path: 'edit/:id', loadComponent: () => import('./pages/hotel/edit/edit.component').then(m => m.HotelEditComponent), canActivate: [roleGuard(['ROLE_ADMIN', 'ROLE_PARTENAIRE'])] }
        ]
      },
      {
        path: 'dashboard',
        canActivate: [authGuard],
        children: [
          { path: '', component: DashboardComponent }, // redirection
          { path: 'admin', loadComponent: () => import('./pages/dashboard/admin-dashboard/admin-dashboard.component').then(m => m.AdminDashboardComponent) },
          { path: 'partenaire', loadComponent: () => import('./pages/dashboard/partenaire-dashboard/partenaire-dashboard.component').then(m => m.PartenaireDashboardComponent) },
          { path: 'client', loadComponent: () => import('./pages/dashboard/client-dashboard/client-dashboard.component').then(m => m.ClientDashboardComponent) }
        ]
      },
      {
        path: 'admin',
        canActivate: [authGuard, roleGuard(['ROLE_ADMIN'])],
        children: [
          { path: 'users', loadComponent: () => import('./pages/admin/users/users.component').then(m => m.UsersComponent) },
          { path: 'users/create', loadComponent: () => import('./pages/admin/create-user/create-user.component').then(m => m.CreateUserComponent) },
          { path: 'users/:id', loadComponent: () => import('./pages/admin/user-details/user-details.component').then(m => m.UserDetailsComponent) },
          { path: 'users/edit/:id', loadComponent: () => import('./pages/admin/edit-user/edit-user.component').then(m => m.EditUserComponent) }
        ]
      },
      {
        path: 'chambre',
        children: [
          { path: 'list/:id', loadComponent: () => import('./pages/chambre/list/list.component').then(m => m.ListComponent) },
          { path: 'details/:id', loadComponent: () => import('./pages/chambre/details/details.component').then(m => m.DetailsComponent) },
          { path: 'add/:id', loadComponent: () => import('./pages/chambre/add/add.component').then(m => m.AddComponent), canActivate: [authGuard, roleGuard(['ROLE_ADMIN', 'ROLE_PARTENAIRE'])] },
          { path: 'edit/:id', loadComponent: () => import('./pages/chambre/edit/edit.component').then(m => m.EditComponent), canActivate: [authGuard, roleGuard(['ROLE_ADMIN', 'ROLE_PARTENAIRE'])] }
        ]
      },
      // Aliases pour éviter de tout casser immédiatement
      { path: 'chambres/:id', redirectTo: 'chambre/details/:id' },
      { path: 'hotels/:id/chambres', redirectTo: 'chambre/list/:id' },
      {
        path: 'profile',
        canActivate: [authGuard],
        children: [
          { path: '', loadComponent: () => import('./pages/user/profile/profile.component').then(m => m.ProfileComponent) },
          { path: 'edit', loadComponent: () => import('./pages/user/profile-edit/profile-edit.component').then(m => m.ProfileEditComponent) },
          { path: 'password', loadComponent: () => import('./pages/user/change-password/change-password.component').then(m => m.ChangePasswordComponent) },
        ]
      },
      {
        path: 'reservations',
        canActivate: [authGuard],
        children: [
          { path: '', redirectTo: 'list', pathMatch: 'full' },
          { path: 'client', loadComponent: () => import('./pages/reservation/list/list.component').then(m => m.ListComponent) },
          { path: 'partenaire', loadComponent: () => import('./pages/reservation/list/list.component').then(m => m.ListComponent) },
          { path: 'admin', loadComponent: () => import('./pages/reservation/list/list.component').then(m => m.ListComponent) },
          { path: 'list', loadComponent: () => import('./pages/reservation/list/list.component').then(m => m.ListComponent) },
          { path: 'add', loadComponent: () => import('./pages/reservation/add/add.component').then(m => m.AddComponent) },
          { path: 'stats', loadComponent: () => import('./pages/reservation/stats/stats.component').then(m => m.StatsComponent), canActivate: [roleGuard(['ROLE_ADMIN', 'ROLE_PARTENAIRE'])] },
          { path: ':id', loadComponent: () => import('./pages/reservation/details/details.component').then(m => m.DetailsComponent) }
        ]
      },
      {
        path: 'reservation',
        canActivate: [authGuard],
        children: [
          { path: 'details/:id', loadComponent: () => import('./pages/reservation/details/details.component').then(m => m.DetailsComponent) }
        ]
      },
      {
        path: 'search',
        loadComponent: () => import('./pages/search/search.component').then(m => m.SearchComponent)
      }
    ]
  },
  { path: '**', redirectTo: '' }
];