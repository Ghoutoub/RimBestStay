import { Component, inject } from '@angular/core';
import { CommonModule, NgFor, NgIf } from '@angular/common';
import { ToastService } from '../../../core/services/toast.service';

@Component({
  selector: 'app-toast',
  standalone: true,
  imports: [CommonModule, NgFor, NgIf],
  templateUrl: './toast.component.html',
})
export class ToastComponent {
  toast = inject(ToastService);

  badgeClass(type: string) {
    switch (type) {
      case 'success': return 'bg-success';
      case 'danger': return 'bg-danger';
      case 'warning': return 'bg-warning text-dark';
      default: return 'bg-info';
    }
  }
}
