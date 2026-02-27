import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface Notification {
    id: number;
    message: string;
    read: boolean;
    createdAt: string;
    type: string;
}

@Injectable({
    providedIn: 'root'
})
export class NotificationService {
    private apiUrl = `${environment.apiBaseUrl}/notifications`;

    constructor(private http: HttpClient) { }

    getUserNotifications(): Observable<Notification[]> {
        return this.http.get<Notification[]>(this.apiUrl);
    }

    getUnreadCount(): Observable<number> {
        return this.http.get<number>(`${this.apiUrl}/unread-count`);
    }

    markAsRead(id: number): Observable<void> {
        return this.http.put<void>(`${this.apiUrl}/${id}/read`, {});
    }

    markAllAsRead(): Observable<void> {
        return this.http.put<void>(`${this.apiUrl}/read-all`, {});
    }
}
