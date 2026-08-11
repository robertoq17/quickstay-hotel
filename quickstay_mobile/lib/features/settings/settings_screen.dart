import 'package:flutter/material.dart';
import '../../core/config/api_config.dart';

class SettingsScreen extends StatefulWidget {
  const SettingsScreen({super.key});

  @override
  State<SettingsScreen> createState() => _SettingsScreenState();
}

class _SettingsScreenState extends State<SettingsScreen> {
  late final TextEditingController _controller;

  @override
  void initState() {
    super.initState();
    _controller = TextEditingController(text: ApiConfig.baseUrl.value);
  }

  @override
  void dispose() {
    _controller.dispose();
    super.dispose();
  }

  void _save() {
    ApiConfig.update(_controller.text);
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(content: Text('URL del Gateway actualizada a ${ApiConfig.baseUrl.value}')),
    );
    Navigator.of(context).pop();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Configuración')),
      body: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            const Text(
              'URL del API Gateway',
              style: TextStyle(fontWeight: FontWeight.bold),
            ),
            const SizedBox(height: 8),
            TextField(
              controller: _controller,
              decoration: const InputDecoration(
                border: OutlineInputBorder(),
                hintText: 'http://192.168.1.23:8000',
              ),
              keyboardType: TextInputType.url,
            ),
            const SizedBox(height: 16),
            Card(
              color: Colors.blue.shade50,
              child: const Padding(
                padding: EdgeInsets.all(12),
                child: Text(
                  '¿Qué poner acá?\n\n'
                  '• Emulador Android: http://10.0.2.2:8000\n'
                  '• Simulador iOS: http://localhost:8000\n'
                  '• Celular físico: http://<IP-LAN-de-tu-PC>:8000 '
                  '(el celular y la PC con el backend deben estar en la '
                  'misma red WiFi)\n\n'
                  'Para encontrar tu IP LAN: "ipconfig" (Windows, buscá '
                  'IPv4) o "ifconfig"/"ip addr" (Mac/Linux).',
                  style: TextStyle(fontSize: 13),
                ),
              ),
            ),
            const SizedBox(height: 24),
            FilledButton(
              onPressed: _save,
              child: const Padding(
                padding: EdgeInsets.symmetric(vertical: 12),
                child: Text('Guardar'),
              ),
            ),
          ],
        ),
      ),
    );
  }
}
