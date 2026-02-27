import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';
import { filter, first } from 'rxjs/operators';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  template: '<div>Redirection...</div>'
})
export class DashboardComponent implements OnInit {
  constructor(private auth: AuthService, private router: Router) { }

  ngOnInit() {
    this.auth.currentUser$
      .pipe(
        filter(user => !!user), // attend que l'utilisateur soit non nul
        first() // ne prend que la première émission
      )
      .subscribe(user => {
        const role = this.auth.getRole(); // ou user.roles[0]?.name
        console.log('🔁 DashboardComponent - rôle après attente :', role);
        if (role === 'ROLE_ADMIN') {
          this.router.navigate(['/dashboard/admin']);
        } else if (role === 'ROLE_PARTENAIRE') {
          this.router.navigate(['/dashboard/partenaire']);
        } else {
          this.router.navigate(['/dashboard/client']);
        }
      });
  }
}