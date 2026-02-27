import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { UserService, UserDTO } from '../../../core/services/user.service';

@Component({
  selector: 'app-create-user',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule],
  templateUrl: './create-user.component.html'
})
export class CreateUserComponent {
  userForm: FormGroup;
  errorMessage: string | null = null; 
  roles = ['ROLE_CLIENT', 'ROLE_PARTENAIRE', 'ROLE_ADMIN'];

  constructor(
    private fb: FormBuilder,
    private userService: UserService,
    private router: Router
  ) {
    this.userForm = this.fb.group({
      nom: ['', Validators.required],
      email: ['', [Validators.required, Validators.email]],
      motDePasse: ['', [Validators.required, Validators.minLength(6)]],
      confirmPassword: ['', Validators.required],
      telephone: [''],
      adresse: [''],
      role: ['ROLE_CLIENT', Validators.required],
      nomEntreprise: [''],
      siret: [''],
      departement: [''],
      actif: [true]
    }, { validators: this.passwordMatchValidator });
  }

  passwordMatchValidator(group: FormGroup) {
    const pwd = group.get('motDePasse')?.value;
    const confirm = group.get('confirmPassword')?.value;
    return pwd === confirm ? null : { mismatch: true };
  }

  onSubmit() {
    if (this.userForm.invalid) return;
    this.errorMessage = null;
    const userData: UserDTO = this.userForm.value;
    this.userService.create(userData).subscribe({
      next: () => this.router.navigate(['/admin/users']),
      error: (err) => {
        console.error('Erreur création utilisateur', err);
        if (err.status === 401) {
          this.errorMessage = 'Vous n\'êtes pas authentifié. Veuillez vous reconnecter.';
        } else if (err.status === 403) {
          this.errorMessage = 'Vous n\'avez pas les droits pour effectuer cette action.';
        } else if (err.status === 400) {
          // Tentative d'extraction du message backend
          const backendMsg = err.error?.message || err.error || 'Données invalides. Vérifiez les champs.';
          this.errorMessage = `Erreur 400 : ${backendMsg}`;
        } else if (err.status === 500) {
          this.errorMessage = 'Erreur serveur. Contactez l\'administrateur.';
        } else {
          this.errorMessage = `Erreur ${err.status} : ${err.statusText}`;
        }
      }
    });
  }
}