import 'dart:io';

import 'package:google_mlkit_barcode_scanning/google_mlkit_barcode_scanning.dart';

class BarcodeService {
  final _scanner = BarcodeScanner();

  Future<List<String>> detectFromPath(String imagePath) async {
    final inputImage = InputImage.fromFile(File(imagePath));
    final barcodes = await _scanner.processImage(inputImage);
    return barcodes
        .map((b) => b.rawValue)
        .whereType<String>()
        .where((v) => v.trim().isNotEmpty)
        .toSet()
        .toList();
  }

  void dispose() {
    _scanner.close();
  }
}
