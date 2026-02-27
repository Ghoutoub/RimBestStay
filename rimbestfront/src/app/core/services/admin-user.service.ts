import { Injectable, inject } from '@angular/core';
import { ApiService } from './api.service';

export type AdminUser = {
  id: number;
  nom?: string;
  prenom?: string;
  email: string;
  role: 'ADMIN' | 'PARTENAIRE' | 'CLIENT';
  enabled: boolean;
  createdAt?: string;
};

export type AdminUserPage = {
  content: AdminUser[];
  totalPages: number;
  number: number;
  totalElements: number;
};

@Injectable({ providedIn: 'root' })
export class AdminUserService {
  private api = inject(ApiService);

  // ✅ GET /api/admin/users?page=&size=&search=&role=&enabled=
  list(params: { page?: number; size?: number; search?: string; role?: string; enabled?: '' | 'true' | 'false' }) {
    const q = new URLSearchParams();
    if (params.page !== undefined) q.set('page', String(params.page));
    if (params.size !== undefined) q.set('size', String(params.size));
    if (params.search) q.set('search', params.search);
    if (params.role) q.set('role', params.role);
    if (params.enabled !== '' && params.enabled !== undefined) q.set('enabled', params.enabled);

    return this.api.get<AdminUserPage>(`/admin/users?${q.toString()}`);
  }

  // ✅ PATCH /api/admin/users/{id}/status
  setEnabled(id: number, enabled: boolean) {
    return this.api.patch(`/admin/users/${id}/status`, { activate: enabled });
  }

  // ⚠️ Le backend n'a pas de /role isolé, il faut un PUT /api/admin/users/{id} complet
  // setRole(id: number, role: AdminUser['role']) {
  //   return this.api.put(`/admin/users/${id}/role`, { role });
  // }
}
