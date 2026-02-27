import { Injectable } from '@angular/core';
import { ApiService } from './api.service';
import { BehaviorSubject, Observable, tap } from 'rxjs';

export interface User {
  id?: number;
  nom: string;
  email: string;
  roles: { name: string }[];
  actif: boolean;
  token?: string;
  telephone?: string;
  adresse?: string;
  createdAt?: Date;
  updatedAt?: Date;
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  private currentUserSubject = new BehaviorSubject<User | null>(null);
  currentUser$ = this.currentUserSubject.asObservable();

  // ✅ Getter pour accéder à la valeur immédiate
  public get currentUserValue(): User | null {
    return this.currentUserSubject.value;
  }

  setUser(updatedUser: any) {
    const current = this.currentUserSubject.value;
    if (current) {
      const newUser = { ...current, ...updatedUser };
      localStorage.setItem('currentUser', JSON.stringify(newUser));
      this.currentUserSubject.next(newUser);
    }
  }
  constructor(private api: ApiService) {
    const stored = localStorage.getItem('currentUser');
    if (stored) {
      this.currentUserSubject.next(JSON.parse(stored));
    }
  }


  private decodeToken(token: string): any {
    try {
      const payload = token.split('.')[1];
      // Remplacer les caractères URL-safe si nécessaire
      const base64 = payload.replace(/-/g, '+').replace(/_/g, '/');
      return JSON.parse(atob(base64));
    } catch (e) {
      console.error('Erreur de décodage du token', e);
      return null;
    }
  }

  login(email: string, password: string): Observable<User> {
    return this.api.post<any>('/auth/login', { email, password }).pipe(
      tap(response => {
        // response contient token, id, email, nom, roles
        const user: User = {
          id: response.id,
          nom: response.nom,
          email: response.email,
          roles: response.roles, // déjà au format [{ name: "ROLE_ADMIN" }]
          token: response.token,
          actif: true
        };
        localStorage.setItem('currentUser', JSON.stringify(user));
        this.currentUserSubject.next(user);
      })
    );
  }

  register(userData: any): Observable<User> {
    // Si le backend attend 'password' et non 'motDePasse'
    const payload = {
      nom: userData.nom,
      email: userData.email,
      password: userData.motDePasse, // mapping
      telephone: userData.telephone,
      adresse: userData.adresse
    };
    return this.api.post<User>('/auth/register', payload);
  }

  logout() {
    localStorage.removeItem('currentUser');
    this.currentUserSubject.next(null);
  }

  getToken(): string | null {
    return this.currentUserSubject.value?.token || null;
  }

  // auth.service.ts (extrait)
  getRole(): string {
    const user = this.currentUserValue;
    if (!user || !user.roles || user.roles.length === 0) return '';
    const role = user.roles[0];
    const result = typeof role === 'string' ? role : role.name;
    console.log('AuthService.getRole() retourne :', result);
    return result;
  }

  hasRole(role: string): boolean {
    const user = this.currentUserSubject.value;
    if (!user || !user.roles) return false;
    const normalizedRole = role.replace('ROLE_', '');
    return user.roles.some(r => r.name.replace('ROLE_', '') === normalizedRole);
  }

  hasAnyRole(roles: string[]): boolean {
    const user = this.currentUserSubject.value;
    if (!user || !user.roles) return false;
    // Normalisation : on enlève le préfixe "ROLE_" pour comparer avec les chaînes passées (ex: "ADMIN" vs "ROLE_ADMIN")
    const userRoles = user.roles.map(r => r.name.replace('ROLE_', ''));
    return roles.some(role => userRoles.includes(role.replace('ROLE_', '')));
  }

  isLoggedIn(): boolean {
    return !!this.currentUserSubject.value;
  }
}