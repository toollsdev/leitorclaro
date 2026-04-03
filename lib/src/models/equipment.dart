class Equipment {
  Equipment({
    this.id,
    required this.barcode,
    required this.equipmentType,
    required this.equipmentName,
    required this.contract,
    required this.capturedAt,
    required this.latitude,
    required this.longitude,
    required this.street,
    required this.neighborhood,
    required this.postalCode,
  });

  final int? id;
  final String barcode;
  final String equipmentType;
  final String equipmentName;
  final String contract;
  final DateTime capturedAt;
  final double latitude;
  final double longitude;
  final String street;
  final String neighborhood;
  final String postalCode;

  Map<String, Object?> toMap() {
    return {
      'id': id,
      'barcode': barcode,
      'equipmentType': equipmentType,
      'equipmentName': equipmentName,
      'contract': contract,
      'capturedAt': capturedAt.toIso8601String(),
      'latitude': latitude,
      'longitude': longitude,
      'street': street,
      'neighborhood': neighborhood,
      'postalCode': postalCode,
    };
  }

  factory Equipment.fromMap(Map<String, Object?> map) {
    return Equipment(
      id: map['id'] as int?,
      barcode: map['barcode'] as String,
      equipmentType: map['equipmentType'] as String,
      equipmentName: map['equipmentName'] as String,
      contract: map['contract'] as String,
      capturedAt: DateTime.parse(map['capturedAt'] as String),
      latitude: (map['latitude'] as num).toDouble(),
      longitude: (map['longitude'] as num).toDouble(),
      street: map['street'] as String,
      neighborhood: map['neighborhood'] as String,
      postalCode: map['postalCode'] as String,
    );
  }
}
