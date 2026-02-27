import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-inscription',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule],
  templateUrl: './inscription.component.html'
})
export class InscriptionComponent {
  form: FormGroup;
  error = '';

  constructor(
    private fb: FormBuilder,
    private auth: AuthService,
    private router: Router
  ) {
    this.form = this.fb.group({
      nom: ['', Validators.required],
      email: ['', [Validators.required, Validators.email]],
      motDePasse: ['', [Validators.required, Validators.minLength(6)]],
      telephone: [''],
      adresse: [''],
      conditions: [false, Validators.requiredTrue]
    });
  }

  onSubmit() {
    if (this.form.invalid) return;
    this.auth.register(this.form.value).subscribe({
      next: () => this.router.navigate(['/login'], { queryParams: { registered: true } }),
      error: (err) => this.error = 'Erreur lors de l\'inscription'
    });
  }
}