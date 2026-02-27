import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { UserService } from '../../../core/services/user.service';

@Component({
    selector: 'app-change-password',
    standalone: true,
    imports: [CommonModule, ReactiveFormsModule, RouterLink],
    template: `
    <div class="max-width-md mx-auto pb-5">
      <div class="d-flex justify-content-between align-items-center mb-5">
        <div>
          <h1 class="h2 fw-bold text-gradient mb-1">Sécurité du Compte</h1>
          <p class="text-muted"><i class="bi bi-shield-lock me-2"></i>Mettez à jour votre mot de passe pour plus de sécurité.</p>
        </div>
        <a class="btn btn-light rounded-pill px-4 shadow-sm" routerLink="/profile">
          <i class="bi bi-arrow-left me-2"></i>Retour
        </a>
      </div>

      <div *ngIf="errorMsg" class="alert alert-danger rounded-4 py-3 border-0 shadow-sm mb-4">
        <i class="bi bi-exclamation-triangle me-2"></i> {{ errorMsg }}
      </div>
      <div *ngIf="okMsg" class="alert alert-success rounded-4 py-3 border-0 shadow-sm mb-4">
        <i class="bi bi-check2-circle me-2"></i> {{ okMsg }}
      </div>

      <div class="premium-card p-0 overflow-hidden shadow-lg mx-auto" style="max-width: 550px;">
        <div class="bg-primary bg-premium-gradient p-4 text-white text-center">
            <div class="bg-white bg-opacity-20 rounded-circle d-inline-flex p-3 mb-3">
                <i class="bi bi-key-fill display-5"></i>
            </div>
            <h4 class="fw-bold mb-0">Modifier mon mot de passe</h4>
        </div>

        <div class="p-4 p-md-5">
          <form [formGroup]="form" (ngSubmit)="submit()">
            <div class="mb-4">
              <label class="form-label fw-bold small text-muted text-uppercase mb-2">Mot de passe actuel</label>
              <div class="input-group">
                <span class="input-group-text bg-white border-end-0 rounded-start-pill ps-3 text-primary"><i class="bi bi-lock"></i></span>
                <input type="password" class="form-control border-start-0 rounded-end-pill py-2 shadow-none ps-1" formControlName="currentPassword" placeholder="Entrez votre mot de passe actuel">
              </div>
            </div>

            <div class="mb-4">
              <label class="form-label fw-bold small text-muted text-uppercase mb-2">Nouveau mot de passe</label>
              <div class="input-group">
                <span class="input-group-text bg-white border-end-0 rounded-start-pill ps-3 text-primary"><i class="bi bi-shield-plus"></i></span>
                <input type="password" class="form-control border-start-0 rounded-end-pill py-2 shadow-none ps-1" formControlName="newPassword" placeholder="Nouveau mot de passe">
              </div>
              <div class="ps-3 mt-2">
                 <small class="text-muted"><i class="bi bi-info-circle me-1"></i> Minimum 6 caractères, recommandé mélange de types.</small>
              </div>
            </div>

            <div class="mb-5">
              <label class="form-label fw-bold small text-muted text-uppercase mb-2">Confirmer le nouveau mot de passe</label>
              <div class="input-group">
                <span class="input-group-text bg-white border-end-0 rounded-start-pill ps-3 text-primary"><i class="bi bi-shield-check"></i></span>
                <input type="password" class="form-control border-start-0 rounded-end-pill py-2 shadow-none ps-1" formControlName="confirmPassword" placeholder="Répétez le nouveau mot de passe">
              </div>
            </div>

            <button type="submit" class="btn btn-primary w-100 py-3 rounded-pill fw-bold shadow-sm d-flex align-items-center justify-content-center" [disabled]="loading || form.invalid">
              <span *ngIf="loading" class="spinner-border spinner-border-sm me-2"></span>
              <i class="bi bi-check-circle me-2" *ngIf="!loading"></i>
              {{ loading ? 'Mise à jour en cours...' : 'Confirmer le changement' }}
            </button>
          </form>
        </div>
      </div>
      
      <div class="mt-4 text-center">
        <p class="text-muted small">Vous avez oublié votre mot de passe ? <a href="#" class="text-primary fw-bold text-decoration-none">Contactez l'administrateur</a></p>
      </div>
    </div>
  `
})
export class ChangePasswordComponent {
    private fb = inject(FormBuilder);
    private userService = inject(UserService);
    private router = inject(Router);

    loading = false;
    errorMsg = '';
    okMsg = '';

    form = this.fb.group({
        currentPassword: ['', Validators.required],
        newPassword: ['', [Validators.required, Validators.minLength(6)]],
        confirmPassword: ['', Validators.required]
    });

    submit() {
        if (this.form.invalid) return;
        if (this.form.value.newPassword !== this.form.value.confirmPassword) {
            this.errorMsg = 'Les mots de passe ne correspondent pas.';
            return;
        }

        this.loading = true;
        this.errorMsg = '';
        this.okMsg = '';

        this.userService.changePassword(this.form.value).subscribe({
            next: () => {
                this.loading = false;
                this.okMsg = 'Mot de passe modifié avec succès !';
                setTimeout(() => this.router.navigate(['/profile']), 2000);
            },
            error: (err) => {
                this.loading = false;
                this.errorMsg = err?.error?.message || 'Erreur lors du changement de mot de passe.';
            }
        });
    }
}
