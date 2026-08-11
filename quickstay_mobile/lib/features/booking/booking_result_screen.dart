import 'package:flutter/material.dart';
import '../../core/models/saga_booking_response.dart';

class BookingResultScreen extends StatelessWidget {
  final SagaBookingResponse response;

  const BookingResultScreen({super.key, required this.response});

  @override
  Widget build(BuildContext context) {
    final success = response.sagaStatus.isSuccess;
    final color = success ? Colors.green : Colors.red;
    final icon = success ? Icons.check_circle : Icons.cancel;

    return Scaffold(
      appBar: AppBar(title: const Text('Resultado')),
      body: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            Icon(icon, color: color, size: 72),
            const SizedBox(height: 12),
            Text(
              success ? '¡Reserva confirmada!' : 'La reserva no se pudo completar',
              textAlign: TextAlign.center,
              style: TextStyle(fontSize: 20, fontWeight: FontWeight.bold, color: color),
            ),
            const SizedBox(height: 8),
            Text(response.message, textAlign: TextAlign.center),
            const SizedBox(height: 24),
            Card(
              child: Padding(
                padding: const EdgeInsets.all(12),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    _row('Saga ID', response.sagaId),
                    _row('Estado del Saga', response.sagaStatus.name),
                    _row('Paso actual', response.currentStep),
                    const Divider(),
                    _row('Reserva', response.reservation.reservationId ?? '—'),
                    _row('Estado reserva', response.reservation.status ?? '—'),
                    const Divider(),
                    _row('Pago', response.payment.paymentId ?? '—'),
                    _row('Estado pago', response.payment.status ?? '—'),
                    if (response.payment.amount != null)
                      _row('Monto', 'Bs ${response.payment.amount!.toStringAsFixed(2)}'),
                  ],
                ),
              ),
            ),
            const SizedBox(height: 24),
            FilledButton(
              onPressed: () => Navigator.of(context).popUntil((r) => r.isFirst),
              child: const Text('Volver a buscar'),
            ),
          ],
        ),
      ),
    );
  }

  Widget _row(String label, String value) {
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 4),
      child: Row(
        mainAxisAlignment: MainAxisAlignment.spaceBetween,
        children: [
          Text(label, style: const TextStyle(color: Colors.black54)),
          Flexible(
            child: Text(
              value,
              textAlign: TextAlign.end,
              style: const TextStyle(fontWeight: FontWeight.w600),
            ),
          ),
        ],
      ),
    );
  }
}
