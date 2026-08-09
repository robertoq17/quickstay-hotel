export interface RoomAvailability {
  roomId: string;
  hotelName: string;
  city: string;
  roomType: string;
  pricePerNight: number;
  capacity: number;
}

export interface RoomSearchParams {
  city: string;
  checkIn: string; // YYYY-MM-DD
  checkOut: string; // YYYY-MM-DD
  maxPrice: number;
}

export interface ReservationRequest {
  roomId: string;
  guestFullName: string;
  guestEmail: string;
  checkIn: string;
  checkOut: string;
}

export interface ReservationResponse {
  reservationId: string;
  roomId: string;
  guestEmail: string;
  checkIn: string;
  checkOut: string;
  status: string;
}

export interface SagaBookingRequest {
  reservation: ReservationRequest;
  paymentAmount: number;
  failPayment: boolean;
}

export interface SagaBookingResponse {
  sagaId: string;
  sagaStatus: string;
  currentStep: string;
  reservation: ReservationResponse | null;
  payment: { paymentId: string; reservationId: string; amount: number; status: string } | null;
  message: string;
}
