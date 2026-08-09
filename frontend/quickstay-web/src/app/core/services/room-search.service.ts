import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { RoomAvailability, RoomSearchParams } from '../models/room.model';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class RoomSearchService {
  private readonly baseUrl = `${environment.apiUrl}/api/rooms`;

  constructor(private http: HttpClient) {}

  search(params: RoomSearchParams): Observable<RoomAvailability[]> {
    const httpParams = new HttpParams()
      .set('city', params.city)
      .set('checkIn', params.checkIn)
      .set('checkOut', params.checkOut)
      .set('maxPrice', params.maxPrice.toString());

    return this.http.get<RoomAvailability[]>(`${this.baseUrl}/search`, { params: httpParams });
  }
}
