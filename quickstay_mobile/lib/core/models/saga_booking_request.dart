/// Espeja com.quickstay.saga.dto.SagaBookingRequest, que a su vez envuelve
/// com.quickstay.booking.dto.ReservationRequest.
class SagaBookingRequest {
  final String roomId;
  final String guestFullName;
  final String guestEmail;
  final DateTime checkIn;
  final DateTime checkOut;
  final double paymentAmount;
  final bool failPayment;

  SagaBookingRequest({
    required this.roomId,
    required this.guestFullName,
    required this.guestEmail,
    required this.checkIn,
    required this.checkOut,
    required this.paymentAmount,
    this.failPayment = false,
  });

  static String _fmtDate(DateTime d) {
    final mm = d.month.toString().padLeft(2, '0');
    final dd = d.day.toString().padLeft(2, '0');
    return '${d.year}-$mm-$dd';
  }

  Map<String, dynamic> toJson() {
    return {
      'reservation': {
        'roomId': roomId,
        'guestFullName': guestFullName,
        'guestEmail': guestEmail,
        'checkIn': _fmtDate(checkIn),
        'checkOut': _fmtDate(checkOut),
      },
      'paymentAmount': paymentAmount,
      'failPayment': failPayment,
    };
  }
}
