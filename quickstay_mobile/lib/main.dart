import 'package:flutter/material.dart';
import 'features/search/search_screen.dart';

void main() {
  runApp(const QuickstayApp());
}

class QuickstayApp extends StatelessWidget {
  const QuickstayApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'QuickStay',
      debugShowCheckedModeBanner: false,
      theme: ThemeData(
        colorScheme: ColorScheme.fromSeed(seedColor: const Color(0xFF0E569E)),
        useMaterial3: true,
      ),
      home: const SearchScreen(),
    );
  }
}
