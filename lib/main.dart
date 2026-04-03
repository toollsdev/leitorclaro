import 'package:flutter/material.dart';
import 'package:image_picker/image_picker.dart';
import 'package:intl/intl.dart';

import 'src/models/equipment.dart';
import 'src/services/barcode_service.dart';
import 'src/services/database_service.dart';
import 'src/services/location_service.dart';

void main() {
  WidgetsFlutterBinding.ensureInitialized();
  runApp(const LeitorClaroApp());
}

class LeitorClaroApp extends StatelessWidget {
  const LeitorClaroApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      debugShowCheckedModeBanner: false,
      title: 'Leitor Claro',
      theme: ThemeData(useMaterial3: true, colorSchemeSeed: Colors.teal),
      home: const HomePage(),
    );
  }
}

class HomePage extends StatefulWidget {
  const HomePage({super.key});

  @override
  State<HomePage> createState() => _HomePageState();
}

class _HomePageState extends State<HomePage> {
  final _barcodeService = BarcodeService();
  final _locationService = LocationService();
  final _typeController = TextEditingController();
  final _nameController = TextEditingController();
  final _contractController = TextEditingController();

  final _picker = ImagePicker();

  bool _loading = false;
  List<String> _detectedCodes = [];
  String? _selectedCode;
  List<Equipment> _equipments = [];

  @override
  void initState() {
    super.initState();
    _loadItems();
  }

  @override
  void dispose() {
    _barcodeService.dispose();
    _typeController.dispose();
    _nameController.dispose();
    _contractController.dispose();
    super.dispose();
  }

  Future<void> _loadItems() async {
    final items = await DatabaseService.instance.fetchEquipments();
    setState(() => _equipments = items);
  }

  Future<void> _takePhotoAndDetect() async {
    final image = await _picker.pickImage(source: ImageSource.camera);
    if (image == null) return;

    setState(() => _loading = true);

    try {
      final codes = await _barcodeService.detectFromPath(image.path);
      setState(() {
        _detectedCodes = codes;
        _selectedCode = codes.isEmpty ? null : codes.first;
      });
      if (codes.isEmpty && mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('Nenhum código de barras detectado.')),
        );
      }
    } catch (e) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text('Erro ao processar imagem: $e')),
        );
      }
    } finally {
      setState(() => _loading = false);
    }
  }

  Future<void> _saveEquipment() async {
    if (_selectedCode == null) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Selecione um código detectado.')),
      );
      return;
    }

    if (_typeController.text.trim().isEmpty || _nameController.text.trim().isEmpty) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Preencha tipo e nome do equipamento.')),
      );
      return;
    }

    setState(() => _loading = true);

    try {
      final address = await _locationService.fetchAddress();

      final equipment = Equipment(
        barcode: _selectedCode!,
        equipmentType: _typeController.text.trim(),
        equipmentName: _nameController.text.trim(),
        contract: _contractController.text.trim(),
        capturedAt: DateTime.now(),
        latitude: address.latitude,
        longitude: address.longitude,
        street: address.street,
        neighborhood: address.neighborhood,
        postalCode: address.postalCode,
      );

      await DatabaseService.instance.insertEquipment(equipment);
      await _loadItems();

      setState(() {
        _detectedCodes = [];
        _selectedCode = null;
        _typeController.clear();
        _nameController.clear();
        _contractController.clear();
      });

      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('Equipamento salvo com sucesso!')),
        );
      }
    } catch (e) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text('Erro ao salvar: $e')),
        );
      }
    } finally {
      setState(() => _loading = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Leitor Claro (.dart)')),
      body: SafeArea(
        child: ListView(
          padding: const EdgeInsets.all(16),
          children: [
            FilledButton.icon(
              onPressed: _loading ? null : _takePhotoAndDetect,
              icon: const Icon(Icons.camera_alt),
              label: const Text('Tirar foto e detectar códigos'),
            ),
            const SizedBox(height: 16),
            if (_detectedCodes.isNotEmpty) ...[
              const Text('Escolha qual código deseja salvar:'),
              const SizedBox(height: 8),
              Wrap(
                spacing: 8,
                runSpacing: 8,
                children: _detectedCodes.map((code) {
                  return ChoiceChip(
                    label: Text(code),
                    selected: _selectedCode == code,
                    onSelected: (_) => setState(() => _selectedCode = code),
                  );
                }).toList(),
              ),
              const SizedBox(height: 16),
            ],
            TextField(
              controller: _typeController,
              decoration: const InputDecoration(labelText: 'Tipo do equipamento'),
            ),
            const SizedBox(height: 12),
            TextField(
              controller: _nameController,
              decoration: const InputDecoration(labelText: 'Nome do equipamento'),
            ),
            const SizedBox(height: 12),
            TextField(
              controller: _contractController,
              decoration: const InputDecoration(labelText: 'Contrato'),
            ),
            const SizedBox(height: 16),
            FilledButton(
              onPressed: _loading ? null : _saveEquipment,
              child: const Text('Salvar equipamento'),
            ),
            if (_loading) ...[
              const SizedBox(height: 16),
              const Center(child: CircularProgressIndicator()),
            ],
            const SizedBox(height: 24),
            Text(
              'Equipamentos salvos',
              style: Theme.of(context).textTheme.titleMedium,
            ),
            const SizedBox(height: 8),
            ..._equipments.map((e) {
              final when = DateFormat('dd/MM/yyyy HH:mm').format(e.capturedAt);
              return Card(
                child: ListTile(
                  title: Text('${e.equipmentName} • ${e.equipmentType}'),
                  subtitle: Text(
                    'Código: ${e.barcode}\n'
                    'Contrato: ${e.contract}\n'
                    'Data/Hora: $when\n'
                    'Endereço: ${e.street}, ${e.neighborhood} - CEP ${e.postalCode}\n'
                    'GPS: ${e.latitude.toStringAsFixed(6)}, ${e.longitude.toStringAsFixed(6)}',
                  ),
                ),
              );
            }),
          ],
        ),
      ),
    );
  }
}
