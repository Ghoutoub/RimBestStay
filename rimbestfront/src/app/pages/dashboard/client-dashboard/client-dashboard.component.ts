import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { DashboardService, DashboardResponse } from '../../../core/services/dashboard.service';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-client-dashboard',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './client-dashboard.component.html'
})
export class ClientDashboardComponent implements OnInit {
  stats: DashboardResponse['stats'] = {};
  recentReservations: any[] = [];
  recommendedHotels: any[] = [];
  userNom = '';

  constructor(
    private dashboardService: DashboardService,
    private auth: AuthService
  ) { }

  ngOnInit() {
    this.userNom = this.auth.currentUserValue?.nom || '';
    this.dashboardService.client().subscribe({
      next: (data) => {
        this.stats = data.stats;
        this.recentReservations = data.lastReservations || [];
        this.recommendedHotels = data.recommendedHotels || [];
      },
      error: (err) => console.error(err)
    });
  }

  logout() {
    this.auth.logout();
    window.location.href = '/login';
  }
}