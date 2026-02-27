import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

export type ToastType = 'success' | 'danger' | 'info' | 'warning';

export type ToastMessage = {
  id: number;
  type: ToastType;
  title?: string;
  message: string;
  timeoutMs?: number;
};

@Injectable({ providedIn: 'root' })
export class ToastService {
  private _toasts$ = new BehaviorSubject<ToastMessage[]>([]);
  toasts$ = this._toasts$.asObservable();
  private seq = 1;

  show(message: string, type: ToastType = 'info', title?: string, timeoutMs = 3500) {
    const toast: ToastMessage = { id: this.seq++, type, title, message, timeoutMs };
    const list = [...this._toasts$.value, toast];
    this._toasts$.next(list);

    if (timeoutMs > 0) {
      setTimeout(() => this.dismiss(toast.id), timeoutMs);
    }
  }

  success(msg: string, title = 'Succès') { this.show(msg, 'success', title); }
  error(msg: string, title = 'Erreur') { this.show(msg, 'danger', title, 4500); }
  info(msg: string, title = 'Info') { this.show(msg, 'info', title); }
  warning(msg: string, title = 'Attention') { this.show(msg, 'warning', title); }

  dismiss(id: number) {
    this._toasts$.next(this._toasts$.value.filter(t => t.id !== id));
  }

  clear() {
    this._toasts$.next([]);
  }
}
