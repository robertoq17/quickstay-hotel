import { CommonModule } from '@angular/common';
import { Component, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RoomAvailability } from '../../core/models/room.model';
import { ReservationService } from '../../core/services/reservation.service';
import { RoomSearchService } from '../../core/services/room-search.service';

@Component({
  selector: 'app-room-search',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './room-search.component.html',
  styleUrl: './room-search.component.scss'
})
export class RoomSearchComponent {
  city = 'Santa Cruz de la Sierra';
  checkIn = '';
  checkOut = '';
  maxPrice = 1000;

  guestFullName = '';
  guestEmail = '';

  results = signal<RoomAvailability[]>([]);
  loading = signal(false);
  errorMessage = signal<string | null>(null);
  confirmationMessage = signal<string | null>(null);

  constructor(
    private roomSearchService: RoomSearchService,
    private reservationService: ReservationService
  ) {}

  search(): void {
    this.errorMessage.set(null);
    this.confirmationMessage.set(null);
    this.loading.set(true);

    this.roomSearchService
      .search({ city: this.city, checkIn: this.checkIn, checkOut: this.checkOut, maxPrice: this.maxPrice })
      .subscribe({
        next: (rooms) => {
          this.results.set(rooms);
          this.loading.set(false);
        },
        error: () => {
          this.errorMessage.set('No se pudo completar la búsqueda. Intenta nuevamente.');
          this.loading.set(false);
        }
      });
  }

  reserve(room: RoomAvailability): void {
    if (!this.guestFullName || !this.guestEmail) {
      this.errorMessage.set('Completa tu nombre y email antes de reservar.');
      return;
    }

    this.reservationService
      .reserve({
        roomId: room.roomId,
        guestFullName: this.guestFullName,
        guestEmail: this.guestEmail,
        checkIn: this.checkIn,
        checkOut: this.checkOut
      })
      .subscribe({
        next: (reservation) => {
          this.confirmationMessage.set(`Reserva confirmada: ${reservation.reservationId}`);
        },
        error: (err) => {
          const msg = err?.error?.message ?? 'No se pudo reservar la habitación.';
          this.errorMessage.set(msg);
        }
      });
  }
}
