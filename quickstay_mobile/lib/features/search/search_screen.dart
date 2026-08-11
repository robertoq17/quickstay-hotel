import 'package:flutter/material.dart';
import '../../core/api/api_client.dart';
import '../../core/api/quickstay_repository.dart';
import '../../core/config/api_config.dart';
import '../../core/models/room_availability.dart';
import '../booking/booking_screen.dart';
import '../saga_status/saga_status_screen.dart';
import '../settings/settings_screen.dart';

class SearchScreen extends StatefulWidget {
  const SearchScreen({super.key});

  @override
  State<SearchScreen> createState() => _SearchScreenState();
}

class _SearchScreenState extends State<SearchScreen> {
  final _cityController = TextEditingController(text: 'Santa Cruz de la Sierra');
  final _maxPriceController = TextEditingController(text: '1000');
  DateTime? _checkIn;
  DateTime? _checkOut;

  bool _loading = false;
  String? _error;
  List<RoomAvailability> _results = [];

  @override
  void initState() {
    super.initState();
    final now = DateTime.now();
    _checkIn = now.add(const Duration(days: 7));
    _checkOut = now.add(const Duration(days: 9));
  }

  @override
  void dispose() {
    _cityController.dispose();
    _maxPriceController.dispose();
    super.dispose();
  }

  Future<void> _pickDate({required bool isCheckIn}) async {
    final initial = isCheckIn ? _checkIn! : _checkOut!;
    final picked = await showDatePicker(
      context: context,
      initialDate: initial,
      firstDate: DateTime.now(),
      lastDate: DateTime.now().add(const Duration(days: 365)),
    );
    if (picked == null) return;
    setState(() {
      if (isCheckIn) {
        _checkIn = picked;
        if (_checkOut!.isBefore(_checkIn!.add(const Duration(days: 1)))) {
          _checkOut = _checkIn!.add(const Duration(days: 1));
        }
      } else {
        _checkOut = picked;
      }
    });
  }

  String _fmt(DateTime d) =>
      '${d.year}-${d.month.toString().padLeft(2, '0')}-${d.day.toString().padLeft(2, '0')}';

  Future<void> _search() async {
    final city = _cityController.text.trim();
    final maxPrice = double.tryParse(_maxPriceController.text.trim());

    if (city.isEmpty || maxPrice == null || _checkIn == null || _checkOut == null) {
      setState(() => _error = 'Completá todos los campos con valores válidos.');
      return;
    }

    setState(() {
      _loading = true;
      _error = null;
    });

    try {
      final results = await QuickstayRepository.searchRooms(
        city: city,
        checkIn: _checkIn!,
        checkOut: _checkOut!,
        maxPrice: maxPrice,
      );
      setState(() {
        _results = results;
        _loading = false;
      });
    } on ApiException catch (e) {
      setState(() {
        _error = e.message;
        _loading = false;
      });
    } catch (e) {
      setState(() {
        _error = 'No se pudo conectar con el Gateway (${ApiConfig.baseUrl.value}). '
            'Revisá la configuración (⚙️) y que backend + api-gateway estén corriendo.';
        _loading = false;
      });
    }
  }

  void _goToBooking(RoomAvailability room) {
    Navigator.of(context).push(
      MaterialPageRoute(
        builder: (_) => BookingScreen(
          room: room,
          checkIn: _checkIn!,
          checkOut: _checkOut!,
        ),
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('QuickStay'),
        actions: [
          IconButton(
            icon: const Icon(Icons.receipt_long),
            tooltip: 'Buscar estado de una reserva (Saga)',
            onPressed: () => Navigator.of(context).push(
              MaterialPageRoute(builder: (_) => const SagaStatusScreen()),
            ),
          ),
          IconButton(
            icon: const Icon(Icons.settings),
            tooltip: 'Configuración',
            onPressed: () => Navigator.of(context).push(
              MaterialPageRoute(builder: (_) => const SettingsScreen()),
            ),
          ),
        ],
      ),
      body: Column(
        children: [
          Padding(
            padding: const EdgeInsets.all(16),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.stretch,
              children: [
                TextField(
                  controller: _cityController,
                  decoration: const InputDecoration(
                    labelText: 'Ciudad',
                    border: OutlineInputBorder(),
                  ),
                ),
                const SizedBox(height: 12),
                Row(
                  children: [
                    Expanded(
                      child: OutlinedButton(
                        onPressed: () => _pickDate(isCheckIn: true),
                        child: Text('Check-in\n${_fmt(_checkIn!)}', textAlign: TextAlign.center),
                      ),
                    ),
                    const SizedBox(width: 12),
                    Expanded(
                      child: OutlinedButton(
                        onPressed: () => _pickDate(isCheckIn: false),
                        child: Text('Check-out\n${_fmt(_checkOut!)}', textAlign: TextAlign.center),
                      ),
                    ),
                  ],
                ),
                const SizedBox(height: 12),
                TextField(
                  controller: _maxPriceController,
                  keyboardType: const TextInputType.numberWithOptions(decimal: true),
                  decoration: const InputDecoration(
                    labelText: 'Precio máximo por noche (Bs)',
                    border: OutlineInputBorder(),
                  ),
                ),
                const SizedBox(height: 12),
                FilledButton.icon(
                  onPressed: _loading ? null : _search,
                  icon: const Icon(Icons.search),
                  label: Text(_loading ? 'Buscando...' : 'Buscar habitaciones'),
                ),
              ],
            ),
          ),
          if (_error != null)
            Padding(
              padding: const EdgeInsets.symmetric(horizontal: 16),
              child: Text(_error!, style: const TextStyle(color: Colors.red)),
            ),
          const Divider(height: 1),
          Expanded(
            child: _results.isEmpty
                ? const Center(child: Text('Buscá para ver habitaciones disponibles'))
                : ListView.builder(
                    itemCount: _results.length,
                    itemBuilder: (context, index) {
                      final room = _results[index];
                      return Card(
                        margin: const EdgeInsets.symmetric(horizontal: 12, vertical: 6),
                        child: ListTile(
                          title: Text('${room.hotelName} — ${room.roomType}'),
                          subtitle: Text(
                            '${room.city} · Capacidad ${room.capacity} · '
                            'Bs ${room.pricePerNight.toStringAsFixed(2)} / noche',
                          ),
                          trailing: FilledButton(
                            onPressed: () => _goToBooking(room),
                            child: const Text('Reservar'),
                          ),
                        ),
                      );
                    },
                  ),
          ),
        ],
      ),
    );
  }
}
