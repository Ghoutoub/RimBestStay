import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { UserService, UserDTO } from '../../../core/services/user.service';

@Component({
    selector: 'app-edit-user',
    standalone: true,
    imports: [CommonModule, ReactiveFormsModule, RouterLink],
    template: `
    <div class="container py-4">
      <div class="d-flex justify-content-between align-items-center mb-4">
        <div>
          <h1 class="h3 mb-0">Modifier l'Utilisateur</h1>
          <p class="text-muted small">ID: #{{ userId }}</p>
        </div>
        <a routerLink="/admin/users" class="btn btn-outline-secondary">
          <i class="bi bi-arrow-left me-1"></i>Retour
        </a>
      </div>

      <div *ngIf="loading" class="text-center py-5">
        <div class="spinner-border text-primary" role="status"></div>
      </div>

      <div *ngIf="errorMessage" class="alert alert-danger mb-4">
        {{ errorMessage }}
      </div>

      <div *ngIf="!loading && userForm" class="card shadow-sm border-0">
        <div class="card-body p-4">
          <form [formGroup]="userForm" (ngSubmit)="onSubmit()">
            <div class="row g-3">
              <div class="col-md-6">
                <label class="form-label">Nom Complet</label>
                <input type="text" class="form-control" formControlName="nom">
              </div>
              <div class="col-md-6">
                <label class="form-label">Email</label>
                <input type="email" class="form-control" formControlName="email">
              </div>
              <div class="col-md-6">
                <label class="form-label">Téléphone</label>
                <input type="text" class="form-control" formControlName="telephone">
              </div>
              <div class="col-md-6">
                <label class="form-label">Rôle</label>
                <select class="form-select" formControlName="role">
                  <option *ngFor="let r of roles" [value]="r">{{ r.replace('ROLE_','') }}</option>
                </select>
              </div>
              <div class="col-12">
                <label class="form-label">Adresse</label>
                <textarea class="form-control" formControlName="adresse"></textarea>
              </div>
            </div>

            <hr class="my-4">

            <div class="d-flex justify-content-end gap-2">
              <a routerLink="/admin/users" class="btn btn-light">Annuler</a>
              <button type="submit" class="btn btn-primary px-4" [disabled]="submitting || userForm.invalid">
                <span *ngIf="submitting" class="spinner-border spinner-border-sm me-1"></span>
                Changer les informations
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  `
})
export class EditUserComponent implements OnInit {
    private fb = inject(FormBuilder);
    private route = inject(ActivatedRoute);
    private router = inject(Router);
    private userService = inject(UserService);

    userId = 0;
    userForm!: FormGroup;
    loading = true;
    submitting = false;
    errorMessage: string | null = null;
    roles = ['ROLE_CLIENT', 'ROLE_PARTENAIRE', 'ROLE_ADMIN'];

    ngOnInit() {
        this.userId = Number(this.route.snapshot.paramMap.get('id'));
        if (!this.userId) {
            this.errorMessage = "ID utilisateur invalide.";
            this.loading = false;
            return;
        }

        this.userService.getById(this.userId).subscribe({
            next: (user) => {
                this.initForm(user);
                this.loading = false;
            },
            error: (err) => {
                this.errorMessage = "Impossible de charger l'utilisateur.";
                this.loading = false;
            }
        });
    }

    initForm(user: any) {
        this.userForm = this.fb.group({
            nom: [user.nom, Validators.required],
            email: [user.email, [Validators.required, Validators.email]],
            telephone: [user.telephone || ''],
            adresse: [user.adresse || ''],
            role: [user.roles[0]?.name || 'ROLE_CLIENT', Validators.required],
            actif: [user.actif]
        });
    }

    onSubmit() {
        if (this.userForm.invalid) return;
        this.submitting = true;
        this.errorMessage = null;

        this.userService.update(this.userId, this.userForm.value).subscribe({
            next: () => {
                this.submitting = false;
                this.router.navigate(['/admin/users']);
            },
            error: (err) => {
                this.submitting = false;
                this.errorMessage = err?.error?.message || "Erreur lors de la mise à jour.";
            }
        });
    }
}
