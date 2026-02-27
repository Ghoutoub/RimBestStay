import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { UserService } from '../../../core/services/user.service';
import { User } from '../../../core/services/auth.service';

@Component({
  selector: 'app-user-details',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './user-details.component.html'
})
export class UserDetailsComponent implements OnInit {
  user: User | null = null;
  isAdmin = false;
  isPartenaire = false;
  isClient = false;

  constructor(
    private route: ActivatedRoute,
    private userService: UserService
  ) {}

  ngOnInit() {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.userService.getById(+id).subscribe(data => {
        this.user = data;
        this.isAdmin = data.roles.some(r => r.name === 'ROLE_ADMIN');
        this.isPartenaire = data.roles.some(r => r.name === 'ROLE_PARTENAIRE');
        this.isClient = data.roles.some(r => r.name === 'ROLE_CLIENT');
      });
    }
  }

  toggleStatus() {
    if (!this.user) return;
    this.userService.toggleStatus(this.user.id!, !this.user.actif).subscribe(updated => {
      this.user = updated;
    });
  }

  deleteUser() {
    if (!this.user) return;
    if (confirm('Supprimer définitivement ?')) {
      this.userService.delete(this.user.id!).subscribe(() => {
        // redirect
      });
    }
  }
}