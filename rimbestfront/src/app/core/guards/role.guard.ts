import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

export const roleGuard = (allowedRoles: string[]): CanActivateFn => {
  return () => {
    const auth = inject(AuthService);
    const router = inject(Router);
    const user = auth.currentUserValue;
    if (user && user.roles.some(r => {
      const roleName = typeof r === 'string' ? r : r.name;
      return allowedRoles.includes(roleName);
    })) {
      return true;
    }
    router.navigate(['/dashboard']);
    return false;
  };
};