import { Injectable } from '@angular/core';
import { ApiService } from './api.service';
import { Observable } from 'rxjs';
import { User } from './auth.service';

export interface UserDTO {
  id?: number;
  nom: string;
  email: string;
  motDePasse?: string;
  confirmPassword?: string;
  telephone?: string;
  adresse?: string;
  role: string; // ROLE_CLIENT, ROLE_PARTENAIRE, ROLE_ADMIN
  // Pour partenaire
  nomEntreprise?: string;
  siret?: string;
  // Pour admin
  departement?: string;
  actif?: boolean;
  createdAt?: Date;
}

export interface Me {
  id?: number;
  nom: string;
  email: string;
  telephone?: string;
  adresse?: string;
  roles?: { name: string }[];
  actif?: boolean;
  // Partenaire fields
  nomEntreprise?: string;
  siret?: string;
  adresseEntreprise?: string;
  telephonePro?: string;
  siteWeb?: string;
  description?: string;
  // Admin fields
  departement?: string;
}

@Injectable({ providedIn: 'root' })
export class UserService {
  constructor(private api: ApiService) { }

  me(): Observable<Me> {
    return this.api.get<Me>('/profile');
  }

  updateMe(profile: Me): Observable<Me> {
    return this.api.put<Me>('/profile', profile);
  }

  getAll(params?: any): Observable<any> {
    return this.api.get<any>('/admin/users', params);
  }

  getById(id: number): Observable<User> {
    return this.api.get<User>(`/admin/users/${id}`);
  }

  create(user: UserDTO): Observable<User> {
    return this.api.post<User>('/admin/users', user);
  }

  update(id: number, user: UserDTO): Observable<User> {
    return this.api.put<User>(`/admin/users/${id}`, user);
  }

  delete(id: number): Observable<void> {
    return this.api.delete<void>(`/admin/users/${id}`);
  }

  toggleStatus(id: number, activate: boolean): Observable<User> {
    return this.api.patch<User>(`/admin/users/${id}/status`, { activate });
  }

  changePassword(data: any): Observable<void> {
    return this.api.put<void>('/profile/password', data);
  }
}