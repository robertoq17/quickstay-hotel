import 'package:flutter/material.dart';
import '../../core/api/api_client.dart';
import '../../core/api/quickstay_repository.dart';
import '../../core/models/room_availability.dart';
import '../../core/models/saga_booking_request.dart';
import 'booking_result_screen.dart';

class BookingScreen extends StatefulWidget {
  final RoomAvailability room;
  final DateTime checkIn;
  final DateTime checkOut;

  const BookingScreen({
    super.key,
    required this.room,
    required this.checkIn,
    required this.checkOut,
  });

  @override
  State<BookingScreen> createState() => _BookingScreenState();
}

class _BookingScreenState extends State<BookingScreen> {
  final _nameController = TextEditingController();
  final _emailController = TextEditingController();
  late final TextEditingController _amountController;

  bool _simulateFailure = false;
  bool _submitting = false;
  String? _error;

  int get _nights => widget.checkOut.difference(widget.checkIn).inDays;

  @override
  void initState() {
    super.initState();
    final total = widget.room.pricePerNight * _nights;
    _amountController = TextEditingController(text: total.toStringAsFixed(2));
  }

  @override
  void dispose() {
    _nameController.dispose();
    _emailController.dispose();
    _amountController.dispose();
    super.dispose();
  }

  Future<void> _submit() async {
    final name = _nameController.text.trim();
    final email = _emailController.text.trim();
    final amount = double.tryParse(_amountController.text.trim());

    if (name.isEmpty || email.isEmpty || amount == null || amount <= 0) {
      setState(() => _error = 'Completá tu nombre, email y un monto válido.');
      return;
    }

    setState(() {
      _submitting = true;
      _error = null;
    });

    final request = SagaBookingRequest(
      roomId: widget.room.roomId,
      guestFullName: name,
      guestEmail: email,
      checkIn: widget.checkIn,
      checkOut: widget.checkOut,
      paymentAmount: amount,
      failPayment: _simulateFailure,
    );

    try {
      // El Saga puede responder 201 (COMPLETED) o 409 (COMPENSATED/FAILED) —
      // en ambos casos el body trae el detalle, así que en la práctica
      // tratamos la respuesta igual (el estado se muestra en la siguiente
      // pantalla). ApiClient solo lanza ApiException si el body no vino en
      // el formato esperado o hubo un error de red/timeout real.
      final response = await QuickstayRepository.bookAndPay(request);
      if (!mounted) return;
      Navigator.of(context).pushReplacement(
        MaterialPageRoute(builder: (_) => BookingResultScreen(response: response)),
      );
    } on ApiException catch (e) {
      setState(() {
        _error = e.message;
        _submitting = false;
      });
    } catch (e) {
      setState(() {
        _error = 'No se pudo completar la reserva. Revisá la conexión con el Gateway.';
        _submitting = false;
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    final room = widget.room;
    return Scaffold(
      appBar: AppBar(title: const Text('Confirmar reserva')),
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            Card(
              child: Padding(
                padding: const EdgeInsets.all(12),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text('${room.hotelName} — ${room.roomType}',
                        style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 16)),
                    const SizedBox(height: 4),
                    Text('${room.city} · Capacidad ${room.capacity}'),
                    Text('$_nights noche(s) · Bs ${room.pricePerNight.toStringAsFixed(2)} / noche'),
                  ],
                ),
              ),
            ),
            const SizedBox(height: 16),
            TextField(
              controller: _nameController,
              decoration: const InputDecoration(
                labelText: 'Nombre completo',
                border: OutlineInputBorder(),
              ),
            ),
            const SizedBox(height: 12),
            TextField(
              controller: _emailController,
              keyboardType: TextInputType.emailAddress,
              decoration: const InputDecoration(
                labelText: 'Email',
                border: OutlineInputBorder(),
              ),
            ),
            const SizedBox(height: 12),
            TextField(
              controller: _amountController,
              keyboardType: const TextInputType.numberWithOptions(decimal: true),
              decoration: const InputDecoration(
                labelText: 'Monto a pagar (Bs)',
                border: OutlineInputBorder(),
              ),
            ),
            const SizedBox(height: 8),
            SwitchListTile(
              contentPadding: EdgeInsets.zero,
              title: const Text('Simular fallo de pago'),
              subtitle: const Text(
                'Para ver la compensación del Saga en acción: la reserva se '
                'crea y después se cancela automáticamente al fallar el pago.',
                style: TextStyle(fontSize: 12),
              ),
              value: _simulateFailure,
              onChanged: (v) => setState(() => _simulateFailure = v),
            ),
            if (_error != null) ...[
              const SizedBox(height: 8),
              Text(_error!, style: const TextStyle(color: Colors.red)),
            ],
            const SizedBox(height: 16),
            FilledButton.icon(
              onPressed: _submitting ? null : _submit,
              icon: const Icon(Icons.payment),
              label: Text(_submitting ? 'Procesando...' : 'Confirmar y pagar'),
            ),
          ],
        ),
      ),
    );
  }
}
