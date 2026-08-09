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
  failPayment = false;

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

    const nights = this.calculateNights();
    if (nights <= 0) {
      this.errorMessage.set('Selecciona fechas válidas de check-in y check-out.');
      return;
    }

    const paymentAmount = room.pricePerNight * nights;
    this.loading.set(true);
    this.errorMessage.set(null);
    this.confirmationMessage.set(null);

    this.reservationService
      .bookAndPay({
        reservation: {
          roomId: room.roomId,
          guestFullName: this.guestFullName,
          guestEmail: this.guestEmail,
          checkIn: this.checkIn,
          checkOut: this.checkOut
        },
        paymentAmount,
        failPayment: this.failPayment
      })
      .subscribe({
        next: (result) => {
          this.loading.set(false);
          this.confirmationMessage.set(
            `${result.message} Saga ${result.sagaId} · Estado: ${result.sagaStatus} · Total: Bs ${paymentAmount}`
          );
        },
        error: (err) => {
          this.loading.set(false);
          const saga = err?.error;
          this.errorMessage.set(saga?.message ?? 'La Saga no pudo completar la reserva.');
        }
      });
  }

  private calculateNights(): number {
    if (!this.checkIn || !this.checkOut) return 0;
    const start = new Date(`${this.checkIn}T00:00:00`);
    const end = new Date(`${this.checkOut}T00:00:00`);
    return Math.round((end.getTime() - start.getTime()) / 86_400_000);
  }
}
