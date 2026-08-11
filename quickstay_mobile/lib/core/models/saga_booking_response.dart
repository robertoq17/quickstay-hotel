/// Espeja com.quickstay.saga.domain.SagaStatus
enum SagaStatus {
  started,
  reservationCreated,
  paymentAuthorized,
  completed,
  compensated,
  failed,
  unknown;

  static SagaStatus fromJson(String? value) {
    switch (value) {
      case 'STARTED':
        return SagaStatus.started;
      case 'RESERVATION_CREATED':
        return SagaStatus.reservationCreated;
      case 'PAYMENT_AUTHORIZED':
        return SagaStatus.paymentAuthorized;
      case 'COMPLETED':
        return SagaStatus.completed;
      case 'COMPENSATED':
        return SagaStatus.compensated;
      case 'FAILED':
        return SagaStatus.failed;
      default:
        return SagaStatus.unknown;
    }
  }

  bool get isSuccess => this == SagaStatus.completed;
}

/// Espeja com.quickstay.booking.dto.ReservationResponse (subset relevante)
class ReservationInfo {
  final String? reservationId;
  final String? roomId;
  final String? guestEmail;
  final String? checkIn;
  final String? checkOut;
  final String? status;

  ReservationInfo({
    this.reservationId,
    this.roomId,
    this.guestEmail,
    this.checkIn,
    this.checkOut,
    this.status,
  });

  factory ReservationInfo.fromJson(Map<String, dynamic>? json) {
    if (json == null) return ReservationInfo();
    return ReservationInfo(
      reservationId: json['reservationId'] as String?,
      roomId: json['roomId'] as String?,
      guestEmail: json['guestEmail'] as String?,
      checkIn: json['checkIn'] as String?,
      checkOut: json['checkOut'] as String?,
      status: json['status'] as String?,
    );
  }
}

/// Espeja com.quickstay.saga.client.PaymentClientResponse
class PaymentInfo {
  final String? paymentId;
  final double? amount;
  final String? status;

  PaymentInfo({this.paymentId, this.amount, this.status});

  factory PaymentInfo.fromJson(Map<String, dynamic>? json) {
    if (json == null) return PaymentInfo();
    return PaymentInfo(
      paymentId: json['paymentId'] as String?,
      amount: (json['amount'] as num?)?.toDouble(),
      status: json['status'] as String?,
    );
  }
}

/// Espeja com.quickstay.saga.dto.SagaBookingResponse
class SagaBookingResponse {
  final String sagaId;
  final SagaStatus sagaStatus;
  final String currentStep;
  final ReservationInfo reservation;
  final PaymentInfo payment;
  final String message;

  SagaBookingResponse({
    required this.sagaId,
    required this.sagaStatus,
    required this.currentStep,
    required this.reservation,
    required this.payment,
    required this.message,
  });

  factory SagaBookingResponse.fromJson(Map<String, dynamic> json) {
    return SagaBookingResponse(
      sagaId: json['sagaId'] as String? ?? '',
      sagaStatus: SagaStatus.fromJson(json['sagaStatus'] as String?),
      currentStep: json['currentStep'] as String? ?? '',
      reservation: ReservationInfo.fromJson(json['reservation'] as Map<String, dynamic>?),
      payment: PaymentInfo.fromJson(json['payment'] as Map<String, dynamic>?),
      message: json['message'] as String? ?? '',
    );
  }
}

/// Espeja com.quickstay.saga.domain.SagaExecution (GET /api/sagas/bookings/{id})
class SagaExecutionInfo {
  final String id;
  final String? reservationId;
  final SagaStatus status;
  final String currentStep;
  final String? errorMessage;
  final String? updatedAt;

  SagaExecutionInfo({
    required this.id,
    this.reservationId,
    required this.status,
    required this.currentStep,
    this.errorMessage,
    this.updatedAt,
  });

  factory SagaExecutionInfo.fromJson(Map<String, dynamic> json) {
    return SagaExecutionInfo(
      id: json['id'] as String? ?? '',
      reservationId: json['reservationId'] as String?,
      status: SagaStatus.fromJson(json['status'] as String?),
      currentStep: json['currentStep'] as String? ?? '',
      errorMessage: json['errorMessage'] as String?,
      updatedAt: json['updatedAt'] as String?,
    );
  }
}
