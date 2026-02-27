import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, finalize, throwError } from 'rxjs';
import { Router } from '@angular/router';

import { AuthService } from '../services/auth.service';
import { LoaderService } from '../services/loader.service';
import { ToastService } from '../services/toast.service';

export const appInterceptor: HttpInterceptorFn = (req, next) => {
  const auth = inject(AuthService);
  const loader = inject(LoaderService);
  const toast = inject(ToastService);
  const router = inject(Router);

  // Token is already injected by authInterceptor — no duplication here
  loader.show();
  return next(req).pipe(
    catchError((err: HttpErrorResponse) => {
      const msg = err?.error?.message || err?.message || 'Erreur serveur.';

      if (err.status === 401) {
        toast.warning('Session expirée. Reconnecte-toi.');
        auth.logout?.();
        router.navigate(['/auth/login'], { queryParams: { returnUrl: router.url } });
      } else if (err.status === 403) {
        toast.error('Accès interdit (403).');
        router.navigate(['/dashboard']);
      } else {
        toast.error(msg);
      }
      return throwError(() => err);
    }),
    finalize(() => loader.hide())
  );
};
