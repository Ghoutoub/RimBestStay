import { Component, inject } from '@angular/core';
import { CommonModule, NgIf } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { UserService, Me } from '../../../core/services/user.service';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-profile-edit',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink, NgIf],
  templateUrl: './profile-edit.component.html',
})
export class ProfileEditComponent {
  private fb = inject(FormBuilder);
  private userService = inject(UserService);
  public auth = inject(AuthService);
  private router = inject(Router);

  loading = false;
  errorMsg = '';
  okMsg = '';
  me: Me | null = null;

  form = this.fb.group({
    nom: ['', [Validators.required, Validators.minLength(2)]],
    email: ['', [Validators.required, Validators.email]],
    telephone: [''],
    adresse: [''],
    nomEntreprise: [''],
    siret: [''],
    adresseEntreprise: [''],
    telephonePro: [''],
    siteWeb: [''],
    description: [''],
    departement: [''],
  });

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
        this.form.patchValue({
          nom: u.nom ?? '',
          email: u.email ?? '',
          telephone: u.telephone ?? '',
          adresse: u.adresse ?? '',
          nomEntreprise: u.nomEntreprise ?? '',
          siret: u.siret ?? '',
          adresseEntreprise: u.adresseEntreprise ?? '',
          telephonePro: u.telephonePro ?? '',
          siteWeb: u.siteWeb ?? '',
          description: u.description ?? '',
          departement: u.departement ?? '',
        });
      },
      error: (err) => {
        this.loading = false;
        this.errorMsg = err?.error?.message || 'Erreur chargement profil.';
      },
    });
  }

  submit() {
    this.errorMsg = '';
    this.okMsg = '';

    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.loading = true;
    this.userService.updateMe(this.form.value as any).subscribe({
      next: (updated: Me) => {
        this.loading = false;
        this.okMsg = 'Profil mis à jour.';
        // ✅ met à jour localStorage pour navbar
        this.auth.setUser(updated);
        // retour profil
        this.router.navigateByUrl('/profile');
      },
      error: (err: any) => {
        this.loading = false;
        this.errorMsg = err?.error?.message || 'Erreur mise à jour profil.';
      },
    });
  }
}
