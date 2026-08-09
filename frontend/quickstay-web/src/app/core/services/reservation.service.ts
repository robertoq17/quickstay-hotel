import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ReservationRequest, ReservationResponse, SagaBookingRequest, SagaBookingResponse } from '../models/room.model';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class ReservationService {
  private readonly baseUrl = `${environment.apiUrl}/api/reservations`;
  private readonly sagaUrl = `${environment.apiUrl}/api/sagas/bookings`;

  constructor(private http: HttpClient) {}

  reserve(request: ReservationRequest): Observable<ReservationResponse> {
    return this.http.post<ReservationResponse>(this.baseUrl, request);
  }

  bookAndPay(request: SagaBookingRequest): Observable<SagaBookingResponse> {
    return this.http.post<SagaBookingResponse>(this.sagaUrl, request);
  }

  cancel(reservationId: string): Observable<ReservationResponse> {
    return this.http.post<ReservationResponse>(`${this.baseUrl}/${reservationId}/cancel`, {});
  }
}
