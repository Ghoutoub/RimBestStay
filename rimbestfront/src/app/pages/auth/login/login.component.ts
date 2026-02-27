import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './login.component.html'
})
export class LoginComponent {
  email = '';
  password = '';
  error = '';

  constructor(private auth: AuthService, private router: Router) { }

  onSubmit() {
    this.auth.login(this.email, this.password).subscribe({
      next: (user) => {
        console.log('✅ Utilisateur reçu du backend :', user);
        console.log('📦 Rôles :', user.roles);
        console.log('🎭 Rôle via getRole() :', this.auth.getRole());
        this.router.navigate(['/dashboard']);
        const storedUser = localStorage.getItem('currentUser');

        if (storedUser) {
          const parsedUser = JSON.parse(storedUser);
          const token: string = parsedUser.token;

          const payload = JSON.parse(atob(token.split('.')[1]));
          console.log(payload);
        }

      },
      error: (err) => {
        console.error('❌ Erreur login :', err);
        this.error = 'Email ou mot de passe incorrect';
      }
    });
  }
}