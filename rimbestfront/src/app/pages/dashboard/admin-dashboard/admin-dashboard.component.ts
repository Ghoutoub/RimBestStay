import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { DashboardService, DashboardResponse } from '../../../core/services/dashboard.service';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './admin-dashboard.component.html'
  
})
export class AdminDashboardComponent implements OnInit {
  stats: DashboardResponse['stats'] = {};
  recentHotels: any[] = []; // à remplacer par un vrai service si besoin
  userNom = '';

  constructor(
    private dashboardService: DashboardService,
    private auth: AuthService
  ) { }

  ngOnInit() {
    this.userNom = this.auth.currentUserValue?.nom || '';
    this.dashboardService.admin().subscribe({
      next: (data) => {
        this.stats = data.stats;
        this.recentHotels = data.lastReservations; // adapter selon le contrat
      },
      error: (err) => console.error(err)
    });
  }

  getRevenuePercentage(): number {
    if (!this.stats.revenueMonth || !this.stats.revenueTarget) return 0;
    return (this.stats.revenueMonth / this.stats.revenueTarget) * 100;
  }

  logout() {
    this.auth.logout();
    window.location.href = '/login';
  }
}