import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ReservationRequest, ReservationResponse } from '../models/room.model';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class ReservationService {
  private readonly baseUrl = `${environment.apiUrl}/api/reservations`;

  constructor(private http: HttpClient) {}

  reserve(request: ReservationRequest): Observable<ReservationResponse> {
    return this.http.post<ReservationResponse>(this.baseUrl, request);
  }

  cancel(reservationId: string): Observable<ReservationResponse> {
    return this.http.post<ReservationResponse>(`${this.baseUrl}/${reservationId}/cancel`, {});
  }
}
