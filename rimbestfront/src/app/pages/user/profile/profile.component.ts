import { Component, inject } from '@angular/core';
import { CommonModule, NgIf } from '@angular/common';
import { RouterLink } from '@angular/router';
import { UserService, Me } from '../../../core/services/user.service';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [CommonModule, RouterLink, NgIf],
  templateUrl: './profile.component.html',
})
export class ProfileComponent {
  private userService = inject(UserService);
  private auth = inject(AuthService);

  loading = false;
  errorMsg = '';
  me: Me | null = null;

  ngOnInit() {
    this.load();
  }

  load() {
    this.loading = true;
    this.errorMsg = '';
    this.userService.me().subscribe({
      next: (u) => {
        this.loading = false;
        this.me = u;
        // ✅ met à jour le user local pour navbar/guards
        this.auth.setUser(u);
      },
      error: (err) => {
        this.loading = false;
        this.errorMsg = err?.error?.message || 'Erreur lors du chargement du profil.';
      },
    });
  }

  get isClient(): boolean {
    return this.me?.roles?.some(r => r.name === 'ROLE_CLIENT') || false;
  }

  get isPartenaire(): boolean {
    return this.me?.roles?.some(r => r.name === 'ROLE_PARTENAIRE') || false;
  }

  get isAdmin(): boolean {
    return this.me?.roles?.some(r => r.name === 'ROLE_ADMIN') || false;
  }
}
