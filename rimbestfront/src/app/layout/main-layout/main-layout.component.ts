import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { AuthService, User } from '../../core/services/auth.service';
import { NotificationService, Notification } from '../../core/services/notification.service';
import { Subscription, interval } from 'rxjs';
import { switchMap, startWith } from 'rxjs/operators';

@Component({
  selector: 'app-main-layout',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './main-layout.component.html',
  styleUrl: './main-layout.component.css'
})
export class MainLayoutComponent implements OnInit, OnDestroy {
  user: User | null = null;
  notifications: Notification[] = [];
  unreadCount: number = 0;
  private notifSub?: Subscription;

  constructor(
    private auth: AuthService,
    private router: Router,
    private notificationService: NotificationService
  ) { }

  ngOnInit() {
    this.auth.currentUser$.subscribe(u => {
      this.user = u;
      if (this.user) {
        this.loadNotifications();
        // Polling every 1 minute
        this.notifSub = interval(60000).subscribe(() => this.loadNotifications());
      } else {
        if (this.notifSub) this.notifSub.unsubscribe();
        this.notifications = [];
        this.unreadCount = 0;
      }
    });
  }

  ngOnDestroy() {
    if (this.notifSub) {
      this.notifSub.unsubscribe();
    }
  }

  loadNotifications() {
    this.notificationService.getUserNotifications().subscribe({
      next: (notifs) => {
        this.notifications = notifs;
        this.unreadCount = notifs.filter(n => !n.read).length;
      },
      error: (err) => console.error('Erreur chargement notifications', err)
    });
  }

  markAsRead(id: number) {
    this.notificationService.markAsRead(id).subscribe({
      next: () => {
        const notif = this.notifications.find(n => n.id === id);
        if (notif && !notif.read) {
          notif.read = true;
          this.unreadCount = Math.max(0, this.unreadCount - 1);
        }
      }
    });
  }

  markAllAsRead() {
    this.notificationService.markAllAsRead().subscribe({
      next: () => {
        this.notifications.forEach(n => n.read = true);
        this.unreadCount = 0;
      }
    });
  }

  logout() {
    this.auth.logout();
    this.router.navigate(['/login']);
  }

  hasRole(role: string): boolean {
    return this.user?.roles?.some(r => r.name === role) ?? false;
  }

  hasAnyRole(roles: string[]): boolean {
    return roles.some(role => this.user?.roles?.some(r => r.name === role) ?? false);
  }
}