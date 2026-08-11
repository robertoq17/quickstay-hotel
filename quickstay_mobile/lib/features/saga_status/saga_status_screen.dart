import 'package:flutter/material.dart';
import '../../core/api/api_client.dart';
import '../../core/api/quickstay_repository.dart';
import '../../core/models/saga_booking_response.dart';

class SagaStatusScreen extends StatefulWidget {
  const SagaStatusScreen({super.key});

  @override
  State<SagaStatusScreen> createState() => _SagaStatusScreenState();
}

class _SagaStatusScreenState extends State<SagaStatusScreen> {
  final _idController = TextEditingController();
  bool _loading = false;
  String? _error;
  SagaExecutionInfo? _result;

  @override
  void dispose() {
    _idController.dispose();
    super.dispose();
  }

  Future<void> _lookup() async {
    final id = _idController.text.trim();
    if (id.isEmpty) {
      setState(() => _error = 'Pegá un Saga ID (lo ves en la pantalla de resultado tras reservar).');
      return;
    }

    setState(() {
      _loading = true;
      _error = null;
      _result = null;
    });

    try {
      final result = await QuickstayRepository.getSagaStatus(id);
      setState(() {
        _result = result;
        _loading = false;
      });
    } on ApiException catch (e) {
      setState(() {
        _error = e.message;
        _loading = false;
      });
    } catch (e) {
      setState(() {
        _error = 'No se pudo consultar el estado del Saga.';
        _loading = false;
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Estado de un Saga')),
      body: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            TextField(
              controller: _idController,
              decoration: const InputDecoration(
                labelText: 'Saga ID',
                border: OutlineInputBorder(),
              ),
            ),
            const SizedBox(height: 12),
            FilledButton(
              onPressed: _loading ? null : _lookup,
              child: Text(_loading ? 'Buscando...' : 'Consultar'),
            ),
            if (_error != null) ...[
              const SizedBox(height: 12),
              Text(_error!, style: const TextStyle(color: Colors.red)),
            ],
            if (_result != null) ...[
              const SizedBox(height: 16),
              Card(
                child: Padding(
                  padding: const EdgeInsets.all(12),
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text('Estado: ${_result!.status.name}',
                          style: const TextStyle(fontWeight: FontWeight.bold)),
                      Text('Paso actual: ${_result!.currentStep}'),
                      if (_result!.reservationId != null)
                        Text('Reserva: ${_result!.reservationId}'),
                      if (_result!.errorMessage != null)
                        Text('Error: ${_result!.errorMessage}',
                            style: const TextStyle(color: Colors.red)),
                    ],
                  ),
                ),
              ),
            ],
          ],
        ),
      ),
    );
  }
}
