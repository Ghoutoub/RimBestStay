import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { UserService } from '../../../core/services/user.service';
import { User } from '../../../core/services/auth.service';

@Component({
  selector: 'app-users',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  templateUrl: './users.component.html'
})
export class UsersComponent implements OnInit {
  users: User[] = [];
  totalUsers = 0;
  clientsCount = 0;
  partenairesCount = 0;
  adminsCount = 0;
  search = '';
  selectedRole = '';
  selectedEnabled: boolean | null = null;
  roles = ['ROLE_CLIENT', 'ROLE_PARTENAIRE', 'ROLE_ADMIN'];

  constructor(private userService: UserService) { }

  ngOnInit() {
    this.loadUsers();
  }

  loadUsers() {
    const params: any = { size: 1000 };
    if (this.search) params.search = this.search;
    if (this.selectedRole) params.role = this.selectedRole;
    if (this.selectedEnabled !== null) params.enabled = this.selectedEnabled;

    this.userService.getAll(params).subscribe({
      next: (data) => {
        // Le backend retourne une Page (data.content) ou une liste directe selon l'endpoint
        const content = data.content || data;
        this.users = content;
        this.totalUsers = data.totalElements || content.length;
        this.clientsCount = content.filter((u: User) => u.roles.some(r => r.name === 'ROLE_CLIENT')).length;
        this.partenairesCount = content.filter((u: User) => u.roles.some(r => r.name === 'ROLE_PARTENAIRE')).length;
        this.adminsCount = content.filter((u: User) => u.roles.some(r => r.name === 'ROLE_ADMIN')).length;
      }
    });
  }

  onFilter() {
    this.loadUsers();
  }

  toggleStatus(id: number, currentStatus: boolean) {
    if (confirm('Changer le statut ?')) {
      this.userService.toggleStatus(id, !currentStatus).subscribe(() => this.loadUsers());
    }
  }

  deleteUser(id: number) {
    if (confirm('Supprimer définitivement ?')) {
      this.userService.delete(id).subscribe(() => this.loadUsers());
    }
  }
}