import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { DashboardService, DashboardResponse } from '../../../core/services/dashboard.service';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-partenaire-dashboard',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './partenaire-dashboard.component.html'
})
export class PartenaireDashboardComponent implements OnInit {
  stats: DashboardResponse['stats'] = {};
  userNom = '';

  constructor(
    private dashboardService: DashboardService,
    private auth: AuthService
  ) {}

  ngOnInit() {
    this.userNom = this.auth.currentUserValue?.nom || '';
    this.dashboardService.partenaire().subscribe({
      next: (data) => {
        this.stats = data.stats;
      },
      error: (err) => console.error(err)
    });
  }

  logout() {
    this.auth.logout();
    window.location.href = '/login';
  }
}